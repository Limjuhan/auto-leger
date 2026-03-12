package com.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFormDto {
    private Long id;
    private Long categoryId;
    private Integer amount;
    private String merchant;
    private String memo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate txDate;

    private String txTime;   // "HH:mm" 형식 문자열 (HTML time input)
    private String source;
}
