package com.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRequestDto {
    private String rawText;   // SMS 전문
    private String apiKey;    // 간단한 인증키 (무단 저장 방지)
}
