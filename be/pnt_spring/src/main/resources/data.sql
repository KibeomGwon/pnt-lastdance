-- 테이블은 JPA가 자동으로 생성하므로 INSERT 문만 작성합니다.

-- 1. 도둑 등급
INSERT INTO grade_thief (id, name, created_at, updated_at, is_deleted)
VALUES (1, '바늘도둑', NOW(), NOW(), false),
       (2, '좀도둑', NOW(), NOW(), false),
       (3, '소매치기', NOW(), NOW(), false),
       (4, '빈집털이', NOW(), NOW(), false),
       (5, '소도둑', NOW(), NOW(), false),
       (6, '금고털이', NOW(), NOW(), false),
       (7, '은행털이', NOW(), NOW(), false),
       (8, '홍길동', NOW(), NOW(), false),
       (9, '인비져블', NOW(), NOW(), false),
       (10, '괴도', NOW(), NOW(), false),
       (11, '대도', NOW(), NOW(), false) ON CONFLICT (id) DO
UPDATE
    SET name = EXCLUDED.name;

-- 2. 경찰 등급
INSERT INTO grade_police (id, name, created_at, updated_at, is_deleted)
VALUES (1, '순경', NOW(), NOW(), false),
       (2, '경장', NOW(), NOW(), false),
       (3, '경사', NOW(), NOW(), false),
       (4, '경위', NOW(), NOW(), false),
       (5, '경감', NOW(), NOW(), false),
       (6, '경정', NOW(), NOW(), false),
       (7, '총경', NOW(), NOW(), false),
       (8, '경무관', NOW(), NOW(), false),
       (9, '치안감', NOW(), NOW(), false),
       (10, '치안정감', NOW(), NOW(), false),
       (11, '치안총감', NOW(), NOW(), false) ON CONFLICT (id) DO
UPDATE
    SET name = EXCLUDED.name;

-- 3. 미션 리스트
INSERT INTO mission (id, title, description, keyword, created_at, updated_at, is_deleted)
VALUES
    (1, '맨홀 촬영하기', '경찰의 포위망이 좁혀오고 있습니다! 주변의 맨홀을 촬영하여 하수도를 통해 탈출 경로를 확보하세요.', 'manhole', NOW(), NOW(), false),
    (2, '자전거 촬영하기', '신속한 이동이 필요합니다! 근처의 자전거를 찾아 촬영하고 도주 수단을 확보하세요.', 'bicycle', NOW(), NOW(), false),
    (3, '오토바이 촬영하기', '추격전이 예상됩니다! 오토바이를 촬영하여 더 빠른 속도로 도주하세요.', 'motorcycle', NOW(), NOW(), false),
    (4, '자동차 촬영하기', '강력한 이동 수단이 필요합니다! 차량을 촬영하여 안전한 은신처까지 거리를 벌리세요.', 'car', NOW(), NOW(), false),
    (5, '자판기 촬영하기', '도주 중 목이 마르군요! 자판기를 촬영하여 갈증을 해소하고 잠시 숨을 고르세요.', 'vending_machine', NOW(), NOW(), false),
    (6, '강아지 촬영하기', '경찰견의 감시가 심합니다! 주변의 강아지를 촬영하여 경찰견의 주의를 돌리세요.', 'dog', NOW(), NOW(), false),
    (7, '고양이 촬영하기', '지붕 위를 자유롭게 다니는 고양이처럼, 고양이를 촬영하여 은밀한 이동 경로를 찾아내세요.', 'cat', NOW(), NOW(), false),
    (8, '가로등 촬영하기', '밤이 깊어갑니다! 가로등을 촬영하여 어둠 속에서 시야를 확보하고 동태를 살피세요.', 'street_light', NOW(), NOW(), false),
    (9, '벤치 촬영하기', '잠시 쉬어갈 시간이 필요합니다! 벤치를 촬영하여 행인인 척 위장해 경찰을 따돌리세요.', 'bench', NOW(), NOW(), false),
    (10, 'CCTV 촬영하기', '감시 카메라에 노출되었습니다! CCTV를 촬영하여 시스템을 무력화하고 흔적을 지우세요.', 'cctv', NOW(), NOW(), false),
    (11, '축구 골대 촬영하기', '경찰의 포위망이 좁혀오고 있습니다! 주변의 축구 골대를 촬영하여 당신의 위치를 추적하지 못하게 만드세요.', 'soccer_goal', NOW(), NOW(), false),
    (12, '농구 골대 촬영하기', '경찰의 포위망이 좁혀오고 있습니다! 주변의 농구 골대를 촬영하여 당신의 위치를 추적하지 못하게 만드세요.', 'basketball_hoop', NOW(), NOW(), false),
    (13, '경찰차 촬영하기', '가장 위험한 곳이 가장 안전한 법! 경찰차를 촬영하여 대범함을 증명하고 수사망을 비웃어주세요.', 'police_car', NOW(), NOW(), false),
    (14, '스타벅스 로고 촬영하기', '도심 속 은신처가 필요합니다. 스타벅스를 촬영하고 수많은 인파 속에 자연스럽게 섞이세요.', 'starbucks', NOW(), NOW(), false),
    (15, '버스 촬영하기', '복잡한 노선만큼 확실한 도주로는 없습니다. 버스를 촬영하여 추적을 따돌릴 이동 수단을 확보하세요.', 'bus', NOW(), NOW(), false)
ON CONFLICT (id) DO
UPDATE
    SET title = EXCLUDED.title,
    description = EXCLUDED.description,
    keyword = EXCLUDED.keyword;