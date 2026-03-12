# auto_ledger — 현대카드 SMS 자동 가계부

## 프로젝트 개요
현대카드 결제 SMS를 MacroDroid(Android)가 자동 감지 →
백엔드 API로 전송 → 파싱 후 DB 저장되는 **완전 자동화 가계부 웹앱**

- GitHub: https://github.com/Limjuhan/auto-leger
- 배포 URL: https://auto-leger-production.up.railway.app

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Frontend | Thymeleaf + Bootstrap 5 + Chart.js |
| Backend | Java 17 + Spring Boot 3.2.3 |
| ORM | Spring Data JPA (Hibernate 6) |
| DB | MySQL 8.x |
| Build | Maven |
| 배포 | Railway (Docker 멀티스테이지 빌드) |
| 자동화 | MacroDroid (Android) → REST API |

---

## 핵심 기능

### 1. SMS 자동 파싱 (완전 자동화)
```
현대카드 결제 발생
  → SMS 수신
  → MacroDroid가 POST /api/notify 호출 (Form 방식)
  → SmsParserService가 파싱
  → DB 자동 저장
```

### 2. SMS 파싱 지원 형식
**형식 A (레이블 있는 형식)**
```
[현대카드]
일시불 승인
일시 : 25/03/12 14:30
가맹점 : 스타벅스 강남점
금액 : 5,500 원
```
**형식 B (SMS 무레이블 형식)**
```
[현대카드]
일시불승인
홍길동(1234)
03/12 14:30
스타벅스
5,500원
```

### 3. 거래내역 관리 (CRUD)
- 카테고리 분류 (식비/교통/쇼핑/의료/문화/기타)
- 수동 입력/수정/삭제 가능

### 4. 대시보드 & 통계
- 이번 달 총 지출 + 카테고리별 도넛 차트
- 월별 지출 추이 막대 차트

---

## API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/notify` | MacroDroid → SMS 자동 저장 ★ |
| GET | `/` | 대시보드 |
| GET/POST | `/transactions` | 거래내역 목록/저장 |
| GET | `/manual` | SMS 수동 붙여넣기 입력 화면 |
| GET | `/stats` | 월별 통계 |

### /api/notify 요청 형식

**MacroDroid용 (Form 방식 권장 - 줄바꿈 안전)**
```
Content-Type: application/x-www-form-urlencoded
Body: rawText={SMS내용}&apiKey={키}
```

**테스트용 (JSON 방식)**
```json
{
  "rawText": "SMS 전문",
  "apiKey": "설정한 API 키"
}
```

### 응답 예시
```json
// 성공
{"success": true, "message": "저장 완료", "merchant": "스타벅스", "amount": 5500}

// 실패 (파싱 불가)
{"success": false, "message": "금액 또는 가맹점을 인식할 수 없습니다."}

// 인증 실패
{"success": false, "message": "인증 실패: API 키를 확인하세요"}
```

---

## 프로젝트 구조

```
src/main/java/com/ledger/
├── LedgerApplication.java
├── controller/
│   ├── HomeController.java          ← 대시보드 (GET /)
│   ├── TransactionController.java   ← CRUD
│   ├── ManualController.java        ← SMS 수동 입력 화면 (GET/POST /manual)
│   ├── NotifyController.java        ← SMS 자동화 API ★ (POST /api/notify)
│   └── StatsController.java         ← 통계
├── service/
│   ├── TransactionService.java
│   └── SmsParserService.java        ← SMS 파싱 (형식 A/B 모두 지원, @Slf4j 로그)
├── repository/
│   ├── TransactionRepository.java
│   └── CategoryRepository.java
├── entity/
│   ├── Transaction.java             ← 테이블명: ledger_transaction
│   └── Category.java
└── dto/
    ├── TransactionFormDto.java
    ├── ParseResultDto.java
    ├── CategoryStatDto.java
    └── NotifyRequestDto.java        ← JSON 방식 API 요청 바디
```

---

## 배포 환경변수 (Railway)

| 변수명 | 값 / 설명 |
|--------|-----------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JDBC_DATABASE_URL` | `jdbc:mysql://mysql.railway.internal:3306/railway?...` |
| `JDBC_DATABASE_USERNAME` | `root` |
| `JDBC_DATABASE_PASSWORD` | Railway MySQL 비밀번호 |
| `LEDGER_API_KEY` | MacroDroid 인증키 (임의 설정값) |

---

## 로컬 개발 환경
- MySQL 포트: 3307, 계정: root/1234
- DB명: ledger_db
- API 키 로컬 기본값: `my-secret-key-1234`
- 서버 포트: 8080
- 핫 리로드: spring-boot-devtools + thymeleaf.cache=false

---

## Railway DB 외부 접속 (MySQL Workbench)
- Hostname: `switchback.proxy.rlwy.net`
- Port: `17401`
- Username: `root`
- Schema: `railway`
