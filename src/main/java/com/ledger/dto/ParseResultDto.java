package com.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseResultDto {
    private boolean success;
    private Integer amount;
    private String merchant;
    private LocalDate txDate;
    private LocalTime txTime;
    private Long suggestedCategoryId;
    private String rawText;
    private String errorMessage;
}
