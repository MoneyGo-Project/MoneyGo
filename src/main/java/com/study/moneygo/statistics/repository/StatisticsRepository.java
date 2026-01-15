package com.study.moneygo.statistics.repository;

import com.study.moneygo.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Transaction, Long> {

    /**
     * 특정 계좌의 기간별 입금 총액
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.toAccount.id = :accountId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    BigDecimal sumDepositByAccountIdAndPeriod(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 계좌의 기간별 출금 총액
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.fromAccount.id = :accountId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    BigDecimal sumWithdrawalByAccountIdAndPeriod(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 계좌의 기간별 거래 유형별 통계
     */
    @Query("SELECT t.type as type, COALESCE(SUM(t.amount), 0) as amount, COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.fromAccount.id = :accountId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate " +
            "GROUP BY t.type")
    List<Object[]> findCategoryStatisticsByAccountIdAndPeriod(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 계좌의 일별 트렌드 (입금)
     */
    @Query("SELECT DATE(t.createdAt) as date, COALESCE(SUM(t.amount), 0) as amount " +
            "FROM Transaction t " +
            "WHERE t.toAccount.id = :accountId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate " +
            "GROUP BY DATE(t.createdAt) " +
            "ORDER BY DATE(t.createdAt)")
    List<Object[]> findDailyDepositTrend(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 계좌의 일별 트렌드 (출금)
     */
    @Query("SELECT DATE(t.createdAt) as date, COALESCE(SUM(t.amount), 0) as amount " +
            "FROM Transaction t " +
            "WHERE t.fromAccount.id = :accountId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.createdAt >= :startDate AND t.createdAt < :endDate " +
            "GROUP BY DATE(t.createdAt) " +
            "ORDER BY DATE(t.createdAt)")
    List<Object[]> findDailyWithdrawalTrend(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
