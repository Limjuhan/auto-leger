# 💳 auto_ledger — 현대카드 SMS 자동 가계부

> 현대카드 결제 SMS를 Android 자동화 앱이 감지하여 REST API로 전송,
> 파싱 후 DB에 저장되는 **완전 자동화 가계부 웹앱**

🌐 **배포 URL**: https://auto-leger-production.up.railway.app

📦 **GitHub**: https://github.com/Limjuhan/auto-leger

⏱️ **개발 기간**: 1일 (기획 → 구현 → 배포 → 자동화 연동까지)

---

## 🚀 하루 만에 완성한 이유

이 프로젝트는 CluadeCode를 활용해 프롬프트 작성 및 계획 을 통해 **아이디어 구체화부터 실제 배포 및 자동화 연동까지 하루 안에 완료**했습니다.

빠른 개발이 가능했던 핵심 결정들:
- **JPA** 선택 → 반복적인 CRUD SQL 작성 없이 메서드명 기반 쿼리 자동 생성으로 개발 속도 극대화
- **Thymeleaf** 선택 → 프론트/백 분리 없이 하나의 프로젝트에서 화면까지 완성
- **Railway** 선택 → GitHub push 한 번으로 빌드·배포 자동화, 인프라 설정 시간 최소화
- **MacroDroid** 선택 → 별도 앱 개발 없이 기존 무료 앱으로 SMS 자동화 완성

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

## 🛠️ 기술 선택 근거

### Backend — Spring Boot 3.2
> 대안: Django(Python), Node.js/Express

국내 기업 환경의 표준 스택. 풍부한 생태계(Spring Security, JPA, Actuator 등)와 구조화된 레이어 아키텍처(Controller-Service-Repository)로 확장성을 확보하면서도 빠른 개발이 가능.

---

### ORM — Spring Data JPA
> 대안: MyBatis

| | JPA | MyBatis |
|--|-----|---------|
| SQL 작성 | 메서드명으로 자동 생성 | 직접 작성 필요 |
| 생산성 | 단순 CRUD에서 높음 | 복잡한 쿼리에서 유리 |
| 객체 매핑 | 자동 (ORM) | 수동 ResultMap |
| 학습 비용 | 높음 | 낮음 |

이 프로젝트의 쿼리는 월별 조회, 카테고리 집계 수준으로 복잡도가 낮아 **JPA의 자동 쿼리 생성이 개발 속도에 직접적으로 유리**. `findByTxDateBetweenOrderByTxDateDesc()` 한 줄로 정렬된 기간 조회가 가능.

---

### Database — MySQL 8
> 대안: MongoDB(NoSQL), PostgreSQL

거래 데이터와 카테고리 간의 **명확한 관계(N:1)** 가 존재하고, 금액·날짜 기반 집계 쿼리가 필요한 구조. 스키마가 고정적이므로 관계형 DB가 적합. Railway에서 MySQL을 공식 서비스로 제공하여 별도 설정 없이 연동 가능.

---

### Frontend — Thymeleaf
> 대안: React, Vue.js

React/Vue는 별도 프론트엔드 서버 구성 및 API 통신 설계가 필요해 **하루 개발에서 오버스펙**. Thymeleaf는 Spring Boot와 네이티브 통합되어 Controller에서 Model에 데이터를 담으면 바로 HTML에 바인딩되므로 빠른 화면 구성 가능.

---

### 배포 — Railway + Docker
> 대안: AWS EC2, Heroku, GCP

| | Railway | AWS EC2 | Heroku |
|--|---------|---------|--------|
| GitHub 자동 배포 | ✅ | 별도 설정 | ✅ |
| MySQL 내장 | ✅ | 별도 RDS | ❌ |
| 무료 플랜 | $5 크레딧/월 | 없음 | 유료화 |
| 초기 설정 난이도 | 낮음 | 높음 | 낮음 |

Docker 멀티스테이지 빌드로 로컬/배포 환경 차이를 없애고, Railway의 GitHub 연동으로 `git push` 한 번에 자동 재배포.

---

## 💡 개발 중 해결한 문제들

### 1. MacroDroid JSON 줄바꿈 오류
SMS 본문의 개행 문자(`\n`)가 JSON 문자열에 이스케이프 없이 삽입되어 HTTP 400 발생.
→ `application/x-www-form-urlencoded` 방식으로 전환. URL 인코딩이 자동으로 처리됨.
→ 기존 JSON 방식은 별도 엔드포인트(`consumes`로 분기)로 유지해 테스트 편의성 확보.

### 2. SMS 형식 다양성 대응
현대카드 SMS는 레이블 있는 형식(`가맹점: XXX`)과 무레이블 형식 두 가지가 존재.
→ 레이블 방식 우선 파싱 후, 실패 시 "금액 줄 앞 줄 역탐색" 폴백 로직으로 두 형식 모두 커버.

### 3. Docker ENTRYPOINT 환경변수 치환 오류
`ENTRYPOINT ["java", "-Dspring.profiles.active=${VAR}"]` exec 형식은 Shell 변수 치환이 안 돼 항상 로컬 프로필로 실행.
→ Spring Boot의 환경변수 자동 바인딩(`SPRING_PROFILES_ACTIVE`) 활용, `-D` 옵션 제거로 해결.

### 4. JPA + data.sql 초기화 순서 문제
Hibernate 테이블 생성 전에 `data.sql`이 실행되어 카테고리 INSERT 실패.
→ `spring.jpa.defer-datasource-initialization: true` 로 테이블 생성 완료 후 SQL 실행 순서 보장.

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

> 기본 설정: MySQL 127.0.0.1:3307 / root / 1234 / API 키 `my-secret-key-1234`
