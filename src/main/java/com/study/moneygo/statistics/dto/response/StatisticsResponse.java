package com.study.moneygo.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {

    // 이번달 총 입금/출금
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdrawal;

    // 저번달 대비 증감률
    private Double depositChangeRate;
    private Double withdrawalChangeRate;

    // 카테고리별 지출 (거래 유형별)
    private List<CategoryStatistics> categoryStatistics;

    // 일별 트렌드 (최근 30일)
    private List<DailyTrend> dailyTrends;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatistics {
        private String category;  // TRANSFER, QR_PAYMENT, SCHEDULED_TRANSFER, DEPOSIT, SELF_DEPOSIT
        private BigDecimal amount;
        private Long count;
        private Double percentage;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;  // yyyy-MM-dd
        private BigDecimal deposit;
        private BigDecimal withdrawal;
    }
}
