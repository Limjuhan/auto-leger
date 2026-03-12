-- JPA가 테이블 자동 생성 후 실행되는 초기 카테고리 데이터
-- application.yml: spring.sql.init.mode=always

INSERT IGNORE INTO category (name, icon, color) VALUES
    ('식비',   '🍽️', '#FF6384'),
    ('교통',   '🚇', '#36A2EB'),
    ('쇼핑',   '🛍️', '#FFCE56'),
    ('의료',   '🏥', '#4BC0C0'),
    ('문화',   '🎬', '#9966FF'),
    ('기타',   '📌', '#FF9F40');
