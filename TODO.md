# TODO: 현대카드 SMS 자동 가계부 (auto_ledger)

목표: 현대카드 SMS → MacroDroid → REST API → DB 자동 저장 완전 자동화 가계부
날짜: 2026-03-12
배포 URL: https://auto-leger-production.up.railway.app

---

## PHASE 1: 프로젝트 세팅
- [x] **1-1** Spring Boot 프로젝트 생성 (Web, Thymeleaf, JPA, MySQL, Lombok, DevTools)
- [x] **1-2** `application.yml` 로컬 DB 설정 (MySQL 3307, root/1234, ledger_db)
- [x] **1-3** `application-prod.yml` Railway 환경변수 설정
- [x] **1-4** `data.sql` 카테고리 초기 데이터 (식비/교통/쇼핑/의료/문화/기타)
- [x] **1-5** JPA ddl-auto=update + defer-datasource-initialization=true 설정
- [x] **1-6** CLAUDE.md 프로젝트 지침서 작성

---

## PHASE 2: 백엔드 - 엔티티/레포지토리
- [x] **2-1** `Category.java` 엔티티 (id/name/icon/color)
- [x] **2-2** `Transaction.java` 엔티티 (@Table(name="ledger_transaction"), @ManyToOne Category)
- [x] **2-3** `CategoryRepository.java`
- [x] **2-4** `TransactionRepository.java` (월별 조회, 카테고리 통계 JPQL)

---

## PHASE 3: 백엔드 - SMS 자동화 API
- [x] **3-1** `SmsParserService.java` 작성
  - 형식 A: 레이블 있는 SMS (가맹점:, 금액:)
  - 형식 B: 무레이블 SMS (금액 줄 앞 줄에서 가맹점 추출)
  - 금액/가맹점/날짜/시간 정규식 추출
  - 키워드 맵으로 카테고리 자동 분류
  - @Slf4j 파싱 성공/실패/예외 로그
- [x] **3-2** `NotifyController.java` - POST `/api/notify`
  - Form 방식 (MacroDroid용, 줄바꿈 URL인코딩으로 안전)
  - JSON 방식 (테스트용)
  - API 키 인증
- [x] **3-3** `NotifyRequestDto.java`
- [x] **3-4** `ManualController.java` - GET/POST `/manual` (SMS 수동 붙여넣기)

---

## PHASE 4: 백엔드 - 거래내역 CRUD
- [x] **4-1** `TransactionService.java` (getList, save, update, delete, 통계)
- [x] **4-2** `TransactionController.java` (목록/새입력/저장/수정/삭제)
- [x] **4-3** `HomeController.java` (대시보드 데이터)
- [x] **4-4** `StatsController.java` (월별 통계)

---

## PHASE 5: 프론트엔드
- [x] **5-1** `fragments/layout.html` (head/navbar/sidebar/scripts fragment)
- [x] **5-2** `index.html` (대시보드 - 도넛 차트 + 최근 내역)
- [x] **5-3** `transaction/manual.html` (SMS 수동 붙여넣기 화면)
- [x] **5-4** `transaction/list.html` (월별 목록, 이전/다음달 네비)
- [x] **5-5** `transaction/form.html` (직접 입력/수정 폼)
- [x] **5-6** `stats/monthly.html` (월별 막대 차트)

---

## PHASE 6: 배포 (Railway)
- [x] **6-1** Dockerfile 멀티스테이지 빌드 작성
- [x] **6-2** GitHub 레포 생성 및 push (https://github.com/Limjuhan/auto-leger)
- [x] **6-3** Railway GitHub 연동 및 MySQL 서비스 추가
- [x] **6-4** Railway 환경변수 5개 설정 (SPRING_PROFILES_ACTIVE, JDBC_*, LEDGER_API_KEY)
- [x] **6-5** 배포 성공 확인 (`Started LedgerApplication`)

---

## PHASE 7: MacroDroid 자동화 연동
- [x] **7-1** MacroDroid 설치 및 매크로 생성
- [x] **7-2** 트리거: SMS 수신 → 액션: HTTP POST /api/notify
- [x] **7-3** Content-Type: application/x-www-form-urlencoded 설정 (JSON 줄바꿈 오류 해결)
- [ ] **7-4** 현대카드 발신번호 필터 추가 (불필요한 API 호출 방지)
- [ ] **7-5** 실제 현대카드 결제 후 대시보드 자동 반영 확인

---

## 진행 현황

| Phase | 작업 | 상태 |
|-------|------|------|
| 1 | 프로젝트 세팅 | ✅ 완료 |
| 2 | 엔티티/레포지토리 | ✅ 완료 |
| 3 | SMS 자동화 API | ✅ 완료 |
| 4 | 거래내역 CRUD | ✅ 완료 |
| 5 | 프론트엔드 | ✅ 완료 |
| 6 | Railway 배포 | ✅ 완료 |
| 7 | MacroDroid 연동 | 🔄 진행 중 (7-4, 7-5 미완) |
