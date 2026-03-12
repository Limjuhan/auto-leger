# 💳 auto_ledger — 현대카드 SMS 자동 가계부

> 현대카드 결제 SMS를 Android 자동화 앱이 감지하여 REST API로 전송,
> 파싱 후 DB에 저장되는 **완전 자동화 가계부 웹앱**

🌐 **배포 URL**: https://auto-leger-production.up.railway.app
📦 **GitHub**: https://github.com/Limjuhan/auto-leger

---

## ⚙️ 자동화 흐름

```
현대카드 결제
    → SMS 수신
    → MacroDroid (Android) → POST /api/notify
    → SmsParserService (정규식 파싱)
    → MySQL 자동 저장
    → 웹 대시보드 반영
```

별도 조작 없이 결제 즉시 가계부에 기록됩니다.

---

## 🛠️ 기술 스택 및 선택 근거

| 영역 | 기술 | 선택 근거 |
|------|------|-----------|
| Backend | Spring Boot 3.2 | 국내 기업 표준 스택, 빠른 REST API 구성 |
| ORM | Spring Data JPA | SQL 직접 작성 없이 객체 중심 DB 접근, 유지보수 용이 |
| Database | MySQL 8 | 관계형 데이터(거래↔카테고리) 표현에 적합, Railway 공식 지원 |
| Frontend | Thymeleaf | 서버사이드 렌더링으로 별도 API 서버 분리 없이 빠른 개발 가능 |
| 차트 | Chart.js | 경량 라이브러리, CDN으로 추가 빌드 설정 불필요 |
| 자동화 | MacroDroid | Android에서 SMS 수신 이벤트를 HTTP 요청으로 연결 가능한 무료 앱 |
| 배포 | Railway | GitHub 연동 자동 배포, MySQL 내장 제공, 무료 크레딧으로 개인 프로젝트에 적합 |
| 컨테이너 | Docker | 로컬/서버 환경 차이 없는 일관된 배포 환경 보장 |

---

## 💡 기술적 문제 해결

### 1. MacroDroid JSON 줄바꿈 오류
SMS 본문의 개행 문자(`\n`)가 JSON 문자열에 그대로 들어가 파싱 오류 발생.
→ `Content-Type: application/x-www-form-urlencoded` 방식으로 전환하여 URL 인코딩으로 해결.
→ 하위 호환을 위해 JSON 방식도 별도 엔드포인트로 유지.

### 2. SMS 형식 다양성 대응
현대카드 SMS는 레이블 있는 형식(가맹점: XXX)과 없는 형식 두 가지가 존재.
→ 레이블 방식 우선 파싱 후, 실패 시 "금액 줄 앞 줄 탐색" 폴백 로직으로 두 형식 모두 지원.

### 3. Docker ENTRYPOINT 환경변수 치환 오류
`ENTRYPOINT ["java", "-Dspring.profiles.active=${VAR}"]` exec 형식은 Shell 변수 치환이 되지 않아 항상 로컬 프로필로 실행됨.
→ Spring Boot의 환경변수 자동 바인딩(`SPRING_PROFILES_ACTIVE`)을 활용하여 `-D` 옵션 제거로 해결.

### 4. JPA 테이블 생성 순서 문제
`data.sql` 실행 시점이 Hibernate 테이블 생성 이전이어서 카테고리 초기 데이터 INSERT 실패.
→ `spring.jpa.defer-datasource-initialization: true` 설정으로 테이블 생성 후 SQL 실행 순서 보장.

---

## 주요 기능

- **SMS 자동 파싱**: 레이블/무레이블 두 가지 현대카드 SMS 형식 모두 지원
- **카테고리 자동 분류**: 가맹점명 키워드 매핑 (식비/교통/쇼핑/의료/문화/기타)
- **대시보드**: 이번 달 총 지출 + 카테고리별 도넛 차트
- **거래내역 관리**: 월별 조회, 수동 입력/수정/삭제
- **월별 통계**: 최근 6개월 지출 추이 막대 차트

---

## 로컬 실행

```bash
# 1. 클론
git clone https://github.com/Limjuhan/auto-leger.git

# 2. MySQL DB 생성 (포트 3307)
CREATE DATABASE ledger_db CHARACTER SET utf8mb4;

# 3. 실행
mvn spring-boot:run

# 4. 접속
http://localhost:8080
```

> `application.yml` 기본 설정: MySQL 127.0.0.1:3307, root/1234, API 키 `my-secret-key-1234`
