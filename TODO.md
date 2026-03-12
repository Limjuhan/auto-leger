# TODO: 하루만에 현대카드 가계부 만들기 (카카오 알림톡 파싱)

목표: 하루(8~10시간) 안에 동작하는 MVP 완성
날짜: 2026-03-12

---

## PHASE 1: 프로젝트 세팅 (약 30분)

- [x] **1-1** Spring Initializr로 프로젝트 생성
  - Dependencies: Spring Web, Thymeleaf, Spring Data JPA, MySQL Driver, Lombok
- [x] **1-2** `application.yml` DB 설정 (로컬 MySQL 접속 정보)
- [x] **1-3** MySQL에 `ledger_db` 데이터베이스 생성
- [x] **1-4** JPA ddl-auto=create 로 테이블 자동 생성 확인
- [x] **1-5** `data.sql` 카테고리 초기 데이터 자동 INSERT 확인
- [x] **1-6** `pom.xml` 의존성 확인 후 빌드 성공 확인

---

## PHASE 2: 백엔드 - 엔티티/레포지토리 (약 45분)

- [x] **2-1** `Category.java` 엔티티 작성 (@Entity, @Id, Lombok)
- [x] **2-2** `Transaction.java` 엔티티 작성 (@ManyToOne Category 관계 포함)
- [x] **2-3** `CategoryRepository.java` 작성 (JpaRepository 상속)
- [x] **2-4** `TransactionRepository.java` 작성
  - findByTxDateBetween (월별 조회)
  - 카테고리별 합계용 @Query
- [x] **2-5** `data.sql` 카테고리 초기 데이터 작성
- [x] **2-6** `application.yml` JPA 설정 (ddl-auto, show-sql)
- [x] **2-7** 앱 기동 후 테이블 자동 생성 확인

---

## PHASE 3: 백엔드 - 카카오 알림톡 파싱 (약 1시간)

- [x] **3-1** `KakaoParserService.java` 작성
  - 현대카드 카카오 알림톡 정규식 파싱
  - 금액, 가맹점명, 날짜, 시간 추출
  - 파싱 실패 시 null 반환 (프론트에서 수동 입력 유도)
- [x] **3-2** 가맹점명 → 카테고리 자동 분류 로직
  - 키워드 맵으로 관리 (식비/교통/쇼핑/의료/문화/기타)
- [x] **3-3** `KakaoController.java` 작성
  - POST `/kakao/parse` → 알림톡 텍스트 받아서 파싱 결과 반환
  - POST `/kakao/save` → 파싱 결과 저장
- [x] **3-4** `KakaoParserService` 단위 테스트 (실제 알림톡 샘플로 검증)

---

## PHASE 4: 백엔드 - 거래내역 CRUD (약 45분)

- [x] **4-1** `TransactionService.java` 작성
  - getList(year, month), getById, save, update, delete
  - getMonthlyStats (대시보드/통계용)
- [x] **4-2** `TransactionController.java` 작성
  - GET `/transactions` → 목록 (월 파라미터)
  - GET `/transactions/new` → 입력 폼
  - POST `/transactions` → 저장
  - GET `/transactions/{id}/edit` → 수정 폼
  - POST `/transactions/{id}` → 수정 (PUT 대신 POST+hidden)
  - POST `/transactions/{id}/delete` → 삭제
- [x] **4-3** `HomeController.java` 작성 → GET `/` → 이번 달 통계 모델 전달

---

## PHASE 5: 프론트엔드 - 레이아웃/공통 (약 30분)

- [x] **5-1** `layout/base.html` 작성 (Thymeleaf Layout Dialect 또는 fragment)
  - Bootstrap 5 CDN 포함
  - Chart.js CDN 포함
  - 상단 네비게이션 바
  - 좌측 사이드바 (대시보드/거래내역/SMS입력/통계)
- [x] **5-2** Bootstrap 5 반응형 그리드 적용

---

## PHASE 6: 프론트엔드 - 화면 구현 (약 2시간)

- [x] **6-1** `index.html` (대시보드)
  - 이번 달 총 지출 카드
  - 카테고리별 도넛 차트 (Chart.js)
  - 최근 거래내역 10건 테이블
- [x] **6-2** `transaction/kakao.html` (카카오 알림톡 입력)
  - textarea → 알림톡 붙여넣기
  - 파싱 결과 미리보기 (금액/가맹점/날짜 표시)
  - 카테고리 선택 드롭다운
  - 저장 버튼
- [x] **6-3** `transaction/list.html` (거래내역 목록)
  - 월 선택 드롭다운 (이전달/다음달 이동)
  - 테이블: 날짜/카테고리/가맹점/금액/메모/수정/삭제
  - 월 합계 표시
- [x] **6-4** `transaction/form.html` (직접 입력/수정)
  - 날짜, 금액, 가맹점명, 카테고리, 메모 입력 폼
- [x] **6-5** `stats/monthly.html` (월별 통계)
  - 월별 지출 막대 차트 (최근 6개월)
  - 카테고리별 지출 순위 테이블

---

## PHASE 7: 연동 및 테스트 (약 1시간)

- [x] **7-1** 실제 현대카드 카카오 알림톡 3~5건으로 파싱 E2E 테스트
- [x] **7-2** 거래내역 직접 입력 → 목록 표시 → 수정 → 삭제 흐름 테스트
- [x] **7-3** 대시보드 차트 정상 표시 확인
- [x] **7-4** 월별 통계 페이지 확인
- [x] **7-5** 파싱 실패 케이스 처리 확인 (오류 메시지 표시)

---

## PHASE 8: 마무리 (약 30분)

- [x] **8-1** 전체 페이지 UI 통일감 점검
- [x] **8-2** 빈 데이터 상태 처리 (거래내역 없을 때 안내 문구)
- [x] **8-3** Railway 배포 설정 (GitHub 연동 + 환경변수 설정)
- [x] **8-4** 배포 후 실제 URL로 접속 확인
- [x] **8-5** README.md 실행/배포 방법 간단 작성

---

## 진행 현황

| Phase | 작업 | 상태 |
|-------|------|------|
| 1 | 프로젝트 세팅 | ✅ 완료 |
| 2 | 엔티티/레포지토리 | ✅ 완료 |
| 3 | 카카오 알림톡 파싱 | ✅ 완료 |
| 4 | 거래내역 CRUD | ✅ 완료 |
| 5 | 레이아웃 | ✅ 완료 |
| 6 | 화면 구현 | ✅ 완료 |
| 7 | 통합 테스트 | ✅ 완료 |
| 8 | 마무리 | ✅ 완료 |
