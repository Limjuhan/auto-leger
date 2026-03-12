package com.ledger.controller;

import com.ledger.dto.NotifyRequestDto;
import com.ledger.dto.ParseResultDto;
import com.ledger.dto.TransactionFormDto;
import com.ledger.service.SmsParserService;
import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MacroDroid(Android)에서 SMS 수신 시 자동 호출하는 REST API
 *
 * [MacroDroid용 - Form 방식]
 * POST /api/notify
 * Content-Type: application/x-www-form-urlencoded
 * Body: rawText={SMS내용}&apiKey={키}
 *
 * [테스트용 - JSON 방식]
 * POST /api/notify
 * Content-Type: application/json
 * Body: { "rawText": "SMS 내용", "apiKey": "설정한키" }
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotifyController {

    private final SmsParserService smsParserService;
    private final TransactionService transactionService;

    @Value("${ledger.api-key}")
    private String configuredApiKey;

    // ── MacroDroid용: Form 방식 (줄바꿈 자동 URL인코딩) ──
    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> notifyForm(
            @RequestParam String rawText,
            @RequestParam String apiKey) {
        return process(rawText, apiKey);
    }

    // ── 테스트용: JSON 방식 ──
    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> notifyJson(@RequestBody NotifyRequestDto request) {
        return process(request.getRawText(), request.getApiKey());
    }

    private ResponseEntity<Map<String, Object>> process(String rawText, String apiKey) {
        // API 키 검증
        if (!configuredApiKey.equals(apiKey)) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "인증 실패: API 키를 확인하세요"));
        }

        if (rawText == null || rawText.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "SMS 내용이 비어있습니다"));
        }

        // 파싱
        ParseResultDto result = smsParserService.parse(rawText);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.getErrorMessage(),
                                 "rawText", rawText));
        }

        // DB 저장
        TransactionFormDto form = TransactionFormDto.builder()
                .amount(result.getAmount())
                .merchant(result.getMerchant())
                .txDate(result.getTxDate())
                .txTime(result.getTxTime() != null ? result.getTxTime().toString() : null)
                .categoryId(result.getSuggestedCategoryId())
                .source("SMS")
                .build();

        transactionService.save(form);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "저장 완료",
                "merchant", result.getMerchant(),
                "amount", result.getAmount()
        ));
    }
}
