package com.ledger.repository;

import com.ledger.dto.CategoryStatDto;
import com.ledger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 월별 거래내역 (날짜 내림차순)
    List<Transaction> findByTxDateBetweenOrderByTxDateDescTxTimeDesc(
            LocalDate start, LocalDate end);

    // 카테고리별 합계 (대시보드 차트용)
    @Query("SELECT new com.ledger.dto.CategoryStatDto(" +
           "COALESCE(c.name, '미분류'), COALESCE(c.color, '#999999'), COALESCE(c.icon, '📌'), SUM(t.amount)) " +
           "FROM Transaction t LEFT JOIN t.category c " +
           "WHERE t.txDate BETWEEN :start AND :end " +
           "GROUP BY c.id, c.name, c.color, c.icon " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategoryStatDto> findCategoryStats(
            @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 월 총액
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.txDate BETWEEN :start AND :end")
    Long sumAmountByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 최근 10건 (대시보드 하단 테이블)
    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    // 월별 합계 (통계 차트 - 최근 6개월)
    @Query("SELECT YEAR(t.txDate), MONTH(t.txDate), SUM(t.amount) " +
           "FROM Transaction t WHERE t.txDate >= :from " +
           "GROUP BY YEAR(t.txDate), MONTH(t.txDate) " +
           "ORDER BY YEAR(t.txDate), MONTH(t.txDate)")
    List<Object[]> findMonthlyTotals(@Param("from") LocalDate from);
}
