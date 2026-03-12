package com.ledger.service;

import com.ledger.dto.ParseResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 현대카드 SMS 파싱 서비스
 *
 * [형식 A - 레이블 있는 형식]
 *   [현대카드]
 *   일시불 승인
 *   일시 : 25/03/12 14:30
 *   가맹점 : 스타벅스 강남점
 *   금액 : 5,500 원
 *
 * [형식 B - SMS 무레이블 형식]
 *   [현대카드]
 *   일시불승인
 *   홍길동(1234)
 *   03/12 14:30
 *   스타벅스
 *   5,500원
 *
 * ※ 실제 SMS 도착 후 형식이 다르면 extractMerchantFromSms() 로직을 조정하세요.
 */
@Slf4j
@Service
public class SmsParserService {

    // 가맹점이 아닌 줄 판별용 스킵 키워드
    private static final List<String> SKIP_KEYWORDS = Arrays.asList(
            "현대카드", "승인", "일시불", "할부", "취소", "web발신", "알림"
    );

    // 카테고리 자동 분류 키워드 (id → 키워드 목록)
    private static final Map<Long, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put(1L, Arrays.asList(
                "스타벅스", "이디야", "투썸", "카페", "커피", "편의점", "GS25", "CU", "세븐일레븐",
                "맥도날드", "버거킹", "롯데리아", "KFC", "피자", "치킨", "배달", "음식", "식당",
                "한식", "중식", "일식", "분식", "김밥", "식품", "마트"
        ));
        CATEGORY_KEYWORDS.put(2L, Arrays.asList(
                "지하철", "버스", "택시", "카카오T", "티머니", "주유", "셀프주유", "SK에너지",
                "GS칼텍스", "현대오일뱅크", "에쓰오일", "고속도로", "하이패스", "주차"
        ));
        CATEGORY_KEYWORDS.put(3L, Arrays.asList(
                "쿠팡", "올리브영", "무신사", "이마트", "홈플러스", "롯데마트", "코스트코",
                "다이소", "신세계", "롯데백화점", "현대백화점", "갤러리아", "마켓컬리", "SSG"
        ));
        CATEGORY_KEYWORDS.put(4L, Arrays.asList(
                "병원", "의원", "약국", "한의원", "치과", "안과", "피부과", "내과", "외과", "클리닉"
        ));
        CATEGORY_KEYWORDS.put(5L, Arrays.asList(
                "CGV", "롯데시네마", "메가박스", "영화", "넷플릭스", "유튜브", "게임", "스팀",
                "교보문고", "YES24", "알라딘", "헬스", "피트니스", "요가", "스포츠"
        ));
    }

    public ParseResultDto parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return ParseResultDto.builder()
                    .success(false)
                    .errorMessage("텍스트를 입력해주세요.")
                    .build();
        }

        try {
            Integer amount   = extractAmount(rawText);
            String  merchant = extractMerchant(rawText);        // 레이블 방식 우선
            if (merchant == null) {
                merchant = extractMerchantFromSms(rawText);     // SMS 무레이블 폴백
            }
            LocalDate txDate = extractDate(rawText);
            LocalTime txTime = extractTime(rawText);

            if (amount == null || merchant == null) {
                log.warn("[SMS 파싱 실패] 금액={}, 가맹점={}, 원문={}", amount, merchant, rawText);
                return ParseResultDto.builder()
                        .success(false)
                        .rawText(rawText)
                        .errorMessage("금액 또는 가맹점을 인식할 수 없습니다. 직접 입력해주세요.")
                        .build();
            }

            log.info("[SMS 파싱 성공] 가맹점={}, 금액={}", merchant, amount);
            return ParseResultDto.builder()
                    .success(true)
                    .amount(amount)
                    .merchant(merchant)
                    .txDate(txDate != null ? txDate : LocalDate.now())
                    .txTime(txTime)
                    .suggestedCategoryId(autoClassify(merchant))
                    .rawText(rawText)
                    .build();

        } catch (Exception e) {
            log.error("[SMS 파싱 예외] 원문={}, 오류={}", rawText, e.getMessage(), e);
            return ParseResultDto.builder()
                    .success(false)
                    .rawText(rawText)
                    .errorMessage("파싱 오류: " + e.getMessage())
                    .build();
        }
    }

    // ─── 금액: 레이블 우선, 없으면 "숫자원" 패턴 ───
    private Integer extractAmount(String text) {
        Pattern[] patterns = {
                Pattern.compile("금액\\s*[：:]*\\s*([\\d,]+)\\s*원"),
                Pattern.compile("이용금액\\s*[：:]*\\s*([\\d,]+)\\s*원"),
                Pattern.compile("승인금액\\s*[：:]*\\s*([\\d,]+)\\s*원"),
                Pattern.compile("([\\d,]+)\\s*원"),
                Pattern.compile("₩([\\d,]+)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                return Integer.parseInt(m.group(1).replaceAll(",", ""));
            }
        }
        return null;
    }

    // ─── 가맹점: 레이블 형식 ───
    private String extractMerchant(String text) {
        Pattern[] patterns = {
                Pattern.compile("가맹점\\s*[：:]+\\s*(.+)"),
                Pattern.compile("이용가맹점\\s*[：:]+\\s*(.+)"),
                Pattern.compile("가맹점명\\s*[：:]+\\s*(.+)"),
                Pattern.compile("상호\\s*[：:]+\\s*(.+)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        }
        return null;
    }

    // ─── 가맹점: SMS 무레이블 폴백 ───
    // 전략: "숫자원" 줄 바로 앞 줄 중 가맹점다운 줄을 선택
    private String extractMerchantFromSms(String text) {
        String[] lines = text.split("\\r?\\n");
        Pattern amountPattern = Pattern.compile("[\\d,]+\\s*원");

        for (int i = 1; i < lines.length; i++) {
            if (amountPattern.matcher(lines[i].trim()).find()) {
                for (int j = i - 1; j >= 0; j--) {
                    String candidate = lines[j].trim();
                    if (!candidate.isBlank() && isMerchantCandidate(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    // 가맹점 후보 줄 판별 (날짜/시간/카드번호/헤더 등 제외)
    private boolean isMerchantCandidate(String line) {
        if (line.startsWith("[") && line.endsWith("]")) return false;       // [현대카드]
        if (line.matches(".*\\d{2}/\\d{2}.*\\d{2}:\\d{2}.*")) return false; // 날짜+시간
        if (line.matches(".*\\(\\d{3,4}\\).*")) return false;               // 카드번호
        if (line.matches("[\\d,]+\\s*원?")) return false;                   // 숫자만
        String lower = line.toLowerCase();
        for (String kw : SKIP_KEYWORDS) {
            if (lower.contains(kw.toLowerCase())) return false;
        }
        return true;
    }

    // ─── 날짜 추출 ───
    private LocalDate extractDate(String text) {
        Matcher m1 = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{2})").matcher(text);
        if (m1.find()) {
            return LocalDate.of(2000 + Integer.parseInt(m1.group(1)),
                    Integer.parseInt(m1.group(2)), Integer.parseInt(m1.group(3)));
        }
        Matcher m2 = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})").matcher(text);
        if (m2.find()) {
            return LocalDate.of(Integer.parseInt(m2.group(1)),
                    Integer.parseInt(m2.group(2)), Integer.parseInt(m2.group(3)));
        }
        Matcher m3 = Pattern.compile("(?<!\\d)(\\d{2})/(\\d{2})(?!/)").matcher(text);
        if (m3.find()) {
            return LocalDate.of(LocalDate.now().getYear(),
                    Integer.parseInt(m3.group(1)), Integer.parseInt(m3.group(2)));
        }
        return null;
    }

    // ─── 시간 추출 ───
    private LocalTime extractTime(String text) {
        Matcher m = Pattern.compile("(\\d{2}):(\\d{2})").matcher(text);
        if (m.find()) {
            return LocalTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }
        return null;
    }

    // ─── 카테고리 자동 분류 ───
    private Long autoClassify(String merchant) {
        String lower = merchant.toLowerCase();
        for (Map.Entry<Long, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword.toLowerCase())) return entry.getKey();
            }
        }
        return 6L; // 기타
    }
}
