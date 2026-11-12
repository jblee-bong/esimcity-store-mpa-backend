-- 카테고리 2단계 (지역 > 나라). 기존 카테고리는 그대로 지역(상위)으로 남음
ALTER TABLE TB_WIFI_CATEGORY
    ADD COLUMN parent_id BIGINT NULL COMMENT '상위(지역) 카테고리. NULL 이면 지역, 값이 있으면 나라' AFTER id,
    ADD KEY idx_parent_id (parent_id);
