package com.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryStatDto {
    private String categoryName;
    private String color;
    private String icon;
    private Long total;
}
