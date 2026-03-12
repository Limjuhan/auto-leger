# 💳 auto_ledger — 현대카드 SMS 자동 가계부

현대카드 결제 문자(SMS)를 MacroDroid가 자동 감지하여 REST API로 전송,
파싱 후 DB에 저장되는 **완전 자동화 가계부 웹앱**

🌐 **배포 URL**: https://auto-leger-production.up.railway.app

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 📲 SMS 자동 파싱 | 현대카드 결제 문자 수신 시 MacroDroid가 자동으로 API 호출 |
| 🗂️ 카테고리 자동 분류 | 가맹점명 키워드 기반으로 식비/교통/쇼핑/의료/문화/기타 분류 |
| 📊 대시보드 | 이번 달 총 지출 + 카테고리별 도넛 차트 |
| 📋 거래내역 | 월별 목록 조회, 수동 입력/수정/삭제 |
| 📈 통계 | 최근 6개월 월별 지출 막대 차트 |
| ✏️ 수동 입력 | SMS 붙여넣기 파싱 또는 직접 입력 지원 |

---

## 🏗️ 아키텍처

```
[현대카드 결제]
      ↓ SMS 수신
[MacroDroid (Android)]
      ↓ POST /api/notify (Form)
[Railway 서버 - Spring Boot]
      ↓ SmsParserService 파싱
[MySQL DB (Railway)]
      ↓
[웹 브라우저 - Thymeleaf]
```

---

## 🛠️ 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.2.3 |
| ORM | Spring Data JPA (Hibernate 6) |
| Database | MySQL 8.x |
| Frontend | Thymeleaf, Bootstrap 5, Chart.js |
| Build | Maven |
| 배포 | Railway (Docker 멀티스테이지 빌드) |
| 자동화 | MacroDroid (Android) |

---

## 🚀 로컬 실행 방법

### 사전 준비
- Java 17+
- MySQL (포트 3307)
- IntelliJ IDEA (권장)

### 1. 레포 클론
```bash
git clone https://github.com/Limjuhan/auto-leger.git
cd auto-leger
```

### 2. MySQL DB 생성
```sql
CREATE DATABASE ledger_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 환경 설정 확인
`src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/ledger_db
    username: root
    password: 1234
```

### 4. 실행
```bash
mvn spring-boot:run
# 또는 IntelliJ에서 LedgerApplication.java 실행
```

### 5. 접속
```
http://localhost:8080
```

---

## 📡 API 명세

### POST /api/notify — SMS 자동 저장

**MacroDroid용 (Form 방식 권장)**
```
Content-Type: application/x-www-form-urlencoded
Body: rawText={SMS내용}&apiKey={키}
```

**테스트용 (JSON 방식)**
```bash
curl -X POST https://auto-leger-production.up.railway.app/api/notify \
  -H "Content-Type: application/json" \
  -d '{"rawText":"[현대카드]\n일시불승인\n홍길동(1234)\n03/12 14:30\n스타벅스\n5500원", "apiKey":"your-api-key"}'
```

**성공 응답 (200)**
```json
{
  "success": true,
  "message": "저장 완료",
  "merchant": "스타벅스",
  "amount": 5500
}
```

**실패 응답 (400)**
```json
{
  "success": false,
  "message": "금액 또는 가맹점을 인식할 수 없습니다."
}
```

---

## 📱 SMS 파싱 지원 형식

**형식 A — 레이블 있는 형식**
```
[현대카드]
일시불 승인
일시 : 25/03/12 14:30
가맹점 : 스타벅스 강남점
금액 : 5,500 원
```

**형식 B — 무레이블 SMS**
```
[현대카드]
일시불승인
홍길동(1234)
03/12 14:30
스타벅스
5,500원
```

---

## ☁️ Railway 배포

### 환경변수 설정 (auto-ledger 서비스)

| 변수명 | 값 |
|--------|-----|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JDBC_DATABASE_URL` | `jdbc:mysql://mysql.railway.internal:3306/railway?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true` |
| `JDBC_DATABASE_USERNAME` | `root` |
| `JDBC_DATABASE_PASSWORD` | Railway MySQL 비밀번호 |
| `LEDGER_API_KEY` | 임의 설정한 API 인증키 |

### 배포 방법
```bash
git push  # GitHub push → Railway 자동 재배포
```

---

## 📲 MacroDroid 설정

1. MacroDroid 앱 설치 (Google Play, 무료)
2. 새 매크로 생성
3. **트리거**: SMS 수신됨 → 발신자: `현대카드`
4. **액션**: HTTP 요청
   - URL: `https://auto-leger-production.up.railway.app/api/notify`
   - Method: `POST`
   - Content-Type: `application/x-www-form-urlencoded`
   - Body: `rawText={sms_body}&apiKey=your-api-key`
5. 저장 및 활성화

---

## 📂 프로젝트 구조

```
auto_ledger/
├── src/main/java/com/ledger/
│   ├── controller/
│   │   ├── HomeController.java       # 대시보드
│   │   ├── TransactionController.java # CRUD
│   │   ├── ManualController.java     # SMS 수동 입력
│   │   ├── NotifyController.java     # SMS 자동화 API ★
│   │   └── StatsController.java      # 통계
│   ├── service/
│   │   ├── SmsParserService.java     # SMS 파싱 핵심 로직 ★
│   │   └── TransactionService.java   # 거래내역 CRUD
│   ├── entity/
│   │   ├── Transaction.java
│   │   └── Category.java
│   ├── repository/
│   ├── dto/
│   └── LedgerApplication.java
├── src/main/resources/
│   ├── templates/
│   │   ├── fragments/layout.html     # 공통 레이아웃
│   │   ├── index.html                # 대시보드
│   │   ├── transaction/
│   │   │   ├── manual.html           # SMS 수동 입력
│   │   │   ├── list.html             # 거래내역 목록
│   │   │   └── form.html             # 직접 입력/수정
│   │   └── stats/monthly.html        # 월별 통계
│   ├── application.yml               # 로컬 설정
│   ├── application-prod.yml          # Railway 설정
│   └── data.sql                      # 카테고리 초기 데이터
├── Dockerfile                        # 멀티스테이지 빌드
├── CLAUDE.md                         # 프로젝트 지침서
└── TODO.md                           # 작업 현황
```

---

## 📄 라이선스

개인 프로젝트 — 비상업적 사용
