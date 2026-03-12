package com.ledger.service;

import com.ledger.dto.CategoryStatDto;
import com.ledger.dto.TransactionFormDto;
import com.ledger.entity.Category;
import com.ledger.entity.Transaction;
import com.ledger.repository.CategoryRepository;
import com.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Transaction> getMonthlyTransactions(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return transactionRepository.findByTxDateBetweenOrderByTxDateDescTxTimeDesc(start, end);
    }

    @Transactional(readOnly = true)
    public long getMonthlyTotal(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        Long total = transactionRepository.sumAmountByDateBetween(start, end);
        return total != null ? total : 0L;
    }

    @Transactional(readOnly = true)
    public List<CategoryStatDto> getMonthlyCategoryStats(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return transactionRepository.findCategoryStats(start, end);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("거래내역을 찾을 수 없습니다: " + id));
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyTotals() {
        LocalDate from = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        return transactionRepository.findMonthlyTotals(from);
    }

    public Transaction save(TransactionFormDto dto) {
        Category category = dto.getCategoryId() != null
                ? categoryRepository.findById(dto.getCategoryId()).orElse(null)
                : null;

        Transaction transaction = Transaction.builder()
                .category(category)
                .amount(dto.getAmount())
                .merchant(dto.getMerchant())
                .memo(dto.getMemo())
                .txDate(dto.getTxDate() != null ? dto.getTxDate() : LocalDate.now())
                .txTime(parseTime(dto.getTxTime()))
                .source(dto.getSource() != null ? dto.getSource() : "MANUAL")
                .build();

        return transactionRepository.save(transaction);
    }

    public Transaction update(Long id, TransactionFormDto dto) {
        Transaction transaction = getById(id);
        Category category = dto.getCategoryId() != null
                ? categoryRepository.findById(dto.getCategoryId()).orElse(null)
                : null;

        transaction.setCategory(category);
        transaction.setAmount(dto.getAmount());
        transaction.setMerchant(dto.getMerchant());
        transaction.setMemo(dto.getMemo());
        transaction.setTxDate(dto.getTxDate());
        transaction.setTxTime(parseTime(dto.getTxTime()));

        return transactionRepository.save(transaction);
    }

    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }
}
