package com.ledger.controller;

import com.ledger.dto.NotifyRequestDto;
import com.ledger.dto.ParseResultDto;
import com.ledger.dto.TransactionFormDto;
import com.ledger.service.SmsParserService;
import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MacroDroid(Android)에서 SMS 수신 시 자동 호출하는 REST API
 *
 * POST /api/notify
 * Body: { "rawText": "SMS 내용", "apiKey": "설정한키" }
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotifyController {

    private final SmsParserService smsParserService;
    private final TransactionService transactionService;

    @Value("${ledger.api-key}")
    private String apiKey;

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notify(@RequestBody NotifyRequestDto request) {

        // API 키 검증
        if (!apiKey.equals(request.getApiKey())) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "인증 실패: API 키를 확인하세요"));
        }

        if (request.getRawText() == null || request.getRawText().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "SMS 내용이 비어있습니다"));
        }

        // 파싱
        ParseResultDto result = smsParserService.parse(request.getRawText());

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.getErrorMessage(),
                                 "rawText", request.getRawText()));
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
