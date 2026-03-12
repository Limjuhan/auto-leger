# auto_ledger — 현대카드 SMS 자동 가계부

## 프로젝트 개요
현대카드 결제 SMS를 MacroDroid(Android)가 자동 감지 →
백엔드 API로 전송 → 파싱 후 DB 저장되는 **완전 자동화 가계부 웹앱**

GitHub: https://github.com/Limjuhan/auto-leger

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Frontend | Thymeleaf + Bootstrap 5 + Chart.js |
| Backend | Java 17 + Spring Boot 3.x |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL 8.x |
| Build | Maven |
| 배포 | Railway (Docker) |
| 자동화 | MacroDroid (Android) → REST API |

---

## 핵심 기능

### 1. SMS 자동 파싱 (완전 자동화)
```
현대카드 결제 발생
  → SMS 수신
  → MacroDroid가 POST /api/notify 호출
  → KakaoParserService가 파싱
  → DB 자동 저장
```

### 2. SMS / 알림톡 파싱 형식
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
| POST | `/api/notify` | MacroDroid → SMS 자동 저장 |
| GET | `/` | 대시보드 |
| GET/POST | `/transactions` | 거래내역 목록/저장 |
| GET | `/kakao` | 수동 붙여넣기 입력 화면 |
| GET | `/stats` | 월별 통계 |

### /api/notify 요청 형식
```json
{
  "rawText": "SMS 전문",
  "apiKey": "설정한 API 키"
}
```

---

## 프로젝트 구조

```
src/main/java/com/ledger/
├── LedgerApplication.java
├── controller/
│   ├── HomeController.java          ← 대시보드
│   ├── TransactionController.java   ← CRUD
│   ├── KakaoController.java         ← 수동 입력 화면
│   ├── NotifyController.java        ← SMS 자동화 API ★
│   └── StatsController.java         ← 통계
├── service/
│   ├── TransactionService.java
│   └── KakaoParserService.java      ← SMS/알림톡 파싱 (형식 A/B 모두 지원)
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
    └── NotifyRequestDto.java        ← API 요청 바디
```

---

## 배포 환경변수 (Railway)

| 변수명 | 설명 |
|--------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JDBC_DATABASE_URL` | Railway MySQL URL |
| `JDBC_DATABASE_USERNAME` | DB 사용자명 |
| `JDBC_DATABASE_PASSWORD` | DB 비밀번호 |
| `LEDGER_API_KEY` | MacroDroid 인증키 (임의 설정) |

---

## 로컬 개발 환경
- MySQL 포트: 3307, 계정: root/1234
- DB명: ledger_db
- API 키 로컬 기본값: `my-secret-key-1234`
- 서버 포트: 8080
