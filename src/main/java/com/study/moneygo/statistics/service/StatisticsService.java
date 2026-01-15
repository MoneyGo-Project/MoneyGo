package com.study.moneygo.statistics.service;

import com.study.moneygo.account.entity.Account;
import com.study.moneygo.account.repository.AccountRepository;
import com.study.moneygo.statistics.dto.response.StatisticsResponse;
import com.study.moneygo.statistics.repository.StatisticsRepository;
import com.study.moneygo.transaction.entity.Transaction;
import com.study.moneygo.user.entity.User;
import com.study.moneygo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * 거래 통계 조회
     */
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("계좌 정보를 찾을 수 없습니다."));

        // 이번달 시작/종료
        LocalDateTime thisMonthStart = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now()
                .plusMonths(1)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // 저번달 시작/종료
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = thisMonthStart;

        // 이번달 총 입금/출금
        BigDecimal thisMonthDeposit = statisticsRepository
                .sumDepositByAccountIdAndPeriod(account.getId(), thisMonthStart, thisMonthEnd);
        BigDecimal thisMonthWithdrawal = statisticsRepository
                .sumWithdrawalByAccountIdAndPeriod(account.getId(), thisMonthStart, thisMonthEnd);

        // 저번달 총 입금/출금
        BigDecimal lastMonthDeposit = statisticsRepository
                .sumDepositByAccountIdAndPeriod(account.getId(), lastMonthStart, lastMonthEnd);
        BigDecimal lastMonthWithdrawal = statisticsRepository
                .sumWithdrawalByAccountIdAndPeriod(account.getId(), lastMonthStart, lastMonthEnd);

        // 증감률 계산
        Double depositChangeRate = calculateChangeRate(lastMonthDeposit, thisMonthDeposit);
        Double withdrawalChangeRate = calculateChangeRate(lastMonthWithdrawal, thisMonthWithdrawal);

        // 카테고리별 통계 (이번달)
        List<Object[]> categoryData = statisticsRepository
                .findCategoryStatisticsByAccountIdAndPeriod(account.getId(), thisMonthStart, thisMonthEnd);

        BigDecimal totalCategoryAmount = categoryData.stream()
                .map(data -> (BigDecimal) data[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StatisticsResponse.CategoryStatistics> categoryStatistics = categoryData.stream()
                .map(data -> {
                    Transaction.TransactionType type = (Transaction.TransactionType) data[0];
                    BigDecimal amount = (BigDecimal) data[1];
                    Long count = (Long) data[2];
                    Double percentage = totalCategoryAmount.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(totalCategoryAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return StatisticsResponse.CategoryStatistics.builder()
                            .category(type.name())
                            .amount(amount)
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        // 일별 트렌드 (최근 30일)
        LocalDateTime last30DaysStart = LocalDateTime.now().minusDays(30)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime today = LocalDateTime.now()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);

        List<Object[]> depositTrendData = statisticsRepository
                .findDailyDepositTrend(account.getId(), last30DaysStart, today);
        List<Object[]> withdrawalTrendData = statisticsRepository
                .findDailyWithdrawalTrend(account.getId(), last30DaysStart, today);

        // 날짜별로 입금/출금 매핑
        Map<String, BigDecimal> depositMap = depositTrendData.stream()
                .collect(Collectors.toMap(
                        data -> data[0].toString(),
                        data -> (BigDecimal) data[1]
                ));

        Map<String, BigDecimal> withdrawalMap = withdrawalTrendData.stream()
                .collect(Collectors.toMap(
                        data -> data[0].toString(),
                        data -> (BigDecimal) data[1]
                ));

        // 최근 30일간의 모든 날짜 생성
        List<StatisticsResponse.DailyTrend> dailyTrends = new ArrayList<>();
        LocalDate currentDate = last30DaysStart.toLocalDate();
        LocalDate endDate = today.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            dailyTrends.add(
                    StatisticsResponse.DailyTrend.builder()
                            .date(dateStr)
                            .deposit(depositMap.getOrDefault(dateStr, BigDecimal.ZERO))
                            .withdrawal(withdrawalMap.getOrDefault(dateStr, BigDecimal.ZERO))
                            .build()
            );
            currentDate = currentDate.plusDays(1);
        }

        log.info("통계 조회 완료: userId={}", user.getId());

        return StatisticsResponse.builder()
                .totalDeposit(thisMonthDeposit)
                .totalWithdrawal(thisMonthWithdrawal)
                .depositChangeRate(depositChangeRate)
                .withdrawalChangeRate(withdrawalChangeRate)
                .categoryStatistics(categoryStatistics)
                .dailyTrends(dailyTrends)
                .build();
    }

    /**
     * 증감률 계산
     */
    private Double calculateChangeRate(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * 현재 사용자 이메일 조회
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
