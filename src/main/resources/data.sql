-- 카테고리 초기 데이터 (앱 시작마다 실행되나 INSERT IGNORE로 중복 방지)
INSERT IGNORE INTO category (id, name, icon, color) VALUES
    (1, '식비',   '🍽️', '#FF6384'),
    (2, '교통',   '🚇', '#36A2EB'),
    (3, '쇼핑',   '🛍️', '#FFCE56'),
    (4, '의료',   '🏥', '#4BC0C0'),
    (5, '문화',   '🎬', '#9966FF'),
    (6, '기타',   '📌', '#FF9F40');
