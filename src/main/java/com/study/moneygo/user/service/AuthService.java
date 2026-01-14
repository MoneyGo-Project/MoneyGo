package com.study.moneygo.user.service;

import com.study.moneygo.account.entity.Account;
import com.study.moneygo.account.repository.AccountRepository;
import com.study.moneygo.simplepassword.dto.request.SimplePasswordChangeRequest;
import com.study.moneygo.user.dto.request.AccountDeleteRequest;
import com.study.moneygo.user.dto.request.LoginRequest;
import com.study.moneygo.user.dto.request.PasswordChangeRequest;
import com.study.moneygo.user.dto.request.SignupRequest;
import com.study.moneygo.user.dto.response.LoginResponse;
import com.study.moneygo.user.dto.response.SignupResponse;
import com.study.moneygo.user.entity.User;
import com.study.moneygo.user.repository.UserRepository;
import com.study.moneygo.util.account.AccountNumberGenerator;
import com.study.moneygo.util.security.JwtTokenProvider;
import com.sun.nio.sctp.IllegalReceiveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 유효성 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .status(User.UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);

        // 계좌 생성하기
        String accountNumber = generateUniqueAccountNumber();
        Account account = Account.builder()
                .user(savedUser)
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        return SignupResponse.of(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다."));
        // 계정 Lock 체크
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new IllegalArgumentException("계정이 잠겨있습니다. 관리자에게 문의하세요.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            // 로그인 성공하면 failedAttempts 초기화 해주기
            resetFailedAttempts(user.getId());

            String token = jwtTokenProvider.generateToken(authentication);
            Long expiresIn = jwtTokenProvider.getExpirationTime();

            // 계좌 정보 조회하기
            Account account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalStateException("계좌 정보를 찾을 수 없습니다."));

            return LoginResponse.of(
                    token,
                    expiresIn,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    account.getAccountNumber()
            );
        } catch (BadCredentialsException e) {
            // 로그인 로직에 실패했으니 failedAttempts 카운트 증가
            incrementFailedAttempts(user.getId());

            System.out.println("===== 로그인 실패 : " + user.getEmail() + " , 실패 횟수 : " + user.getFailedLoginAttempts() + " =====");
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void incrementFailedAttempts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalReceiveException("사용자를 찾을 수 없습니다."));
        user.incrementFailedAttempts();
        userRepository.save(user);

        System.out.println("===== 로그인 실패 : "+ user.getEmail() + ", 실패 횟수 : " + user.getFailedLoginAttempts() + ", 상태 : " + user.getStatus() + " =====");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.resetFailedAttempts();
        userRepository.save(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentUser();

        validateCurrentPassword(request.getCurrentPassword(), user.getPassword());
        validateNewPasswordMatch(request.getNewPassword(), request.getNewPasswordConfirm());
        validatePasswordNotSame(request.getCurrentPassword(), request.getNewPassword());

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", user.getId());
    }

    /**
     * 간편 비밀번호 변경
     */
    @Transactional
    public void changeSimplePassword(SimplePasswordChangeRequest request) {
        User user = getCurrentUser();

        if (!user.hasSimplePassword()) {
            throw new IllegalArgumentException("간편 비밀번호가 등록되어 있지 않습니다.");
        }

        validateCurrentPassword(request.getCurrentSimplePassword(), user.getSimplePassword());
        validateNewPasswordMatch(request.getNewSimplePassword(), request.getNewSimplePasswordConfirm());
        validatePasswordNotSame(request.getCurrentSimplePassword(), request.getNewSimplePassword());

        user.setSimplePassword(passwordEncoder.encode(request.getNewSimplePassword()));
        userRepository.save(user);

        log.info("간편 비밀번호 변경 완료: userId={}", user.getId());
    }

    /**
     * 계정 탈퇴
     */
    @Transactional
    public void deleteAccount(AccountDeleteRequest request) {
        User user = getCurrentUser();

        validateCurrentPassword(request.getPassword(), user.getPassword());

        // 계좌 동결
        accountRepository.findByUserId(user.getId()).ifPresent(account -> {
            account.freeze();
            accountRepository.save(account);
        });

        // 사용자 계정 정지
        user.deactivate();
        userRepository.save(user);

        log.info("계정 탈퇴 완료: userId={}, reason={}", user.getId(), request.getReason());
    }

    // ========== Private 헬퍼 메서드 ==========

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void validateCurrentPassword(String inputPassword, String storedPassword) {
        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateNewPasswordMatch(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validatePasswordNotSame(String currentPassword, String newPassword) {
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("현재 비밀번호와 새 비밀번호가 동일합니다.");
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = AccountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
