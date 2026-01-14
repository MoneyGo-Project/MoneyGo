package com.study.moneygo.user.controller;


import com.study.moneygo.simplepassword.dto.request.SimplePasswordChangeRequest;
import com.study.moneygo.user.dto.request.AccountDeleteRequest;
import com.study.moneygo.user.dto.request.LoginRequest;
import com.study.moneygo.user.dto.request.PasswordChangeRequest;
import com.study.moneygo.user.dto.request.SignupRequest;
import com.study.moneygo.user.dto.response.LoginResponse;
import com.study.moneygo.user.dto.response.SignupResponse;
import com.study.moneygo.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("비밀번호 변경 요청");
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/simple-password")
    public ResponseEntity<Void> changeSimplePassword(@Valid @RequestBody SimplePasswordChangeRequest request) {
        log.info("간편 비밀번호 변경 요청");
        authService.changeSimplePassword(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody AccountDeleteRequest request) {
        log.info("계정 탈퇴 요청");
        authService.deleteAccount(request);
        return ResponseEntity.ok().build();
    }
}
