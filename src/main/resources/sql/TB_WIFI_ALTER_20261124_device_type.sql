-- 운영 TUGE 상품 동기화 (2026-11-24)
-- 1) deviceType 이 620자(73기종)라 100자 칸에서 실패 → TUGE 에서 오는 글자 칸은 넉넉하게 / 목록류는 TEXT
-- 2) 문서에 없던 운영 응답 필드(highSpeed, limitSpeed, countryCodeList ...) 저장 + 원본 JSON 통째 보관(raw_json)
ALTER TABLE TB_WIFI_PRODUCT
    MODIFY tuge_name        VARCHAR(1000) NULL,
    MODIFY product_type     VARCHAR(50)   NULL COMMENT 'DAILY/MONTH/MULTIPLEMONTHS',
    MODIFY data_limited     VARCHAR(10)   NULL,
    MODIFY data_total       VARCHAR(50)   NULL,
    MODIFY data_unit        VARCHAR(20)   NULL,
    MODIFY mcc              TEXT          NULL,
    MODIFY device_type      TEXT          NULL COMMENT '비어있으면 전 기종',
    MODIFY active_type      VARCHAR(50)   NULL,
    MODIFY tuge_modified_dt VARCHAR(50)   NULL,
    ADD COLUMN IF NOT EXISTS high_speed        VARCHAR(50)  NULL COMMENT 'TUGE highSpeed 예: 2048(M) 하루 고속' AFTER rule_desc,
    ADD COLUMN IF NOT EXISTS limit_speed       VARCHAR(50)  NULL COMMENT 'TUGE limitSpeed 예: 2000(K) 소진 후 속도' AFTER high_speed,
    ADD COLUMN IF NOT EXISTS show_grade_number VARCHAR(200) NULL COMMENT 'TUGE showGradeNumber' AFTER limit_speed,
    ADD COLUMN IF NOT EXISTS country_code_list TEXT         NULL COMMENT 'TUGE countryCodeList (ISO)' AFTER show_grade_number,
    ADD COLUMN IF NOT EXISTS period_type       VARCHAR(50)  NULL COMMENT 'TUGE periodType BYDAY/BYMONTH' AFTER country_code_list,
    ADD COLUMN IF NOT EXISTS is_contract       VARCHAR(10)  NULL COMMENT 'TUGE isContract Y/N' AFTER period_type,
    ADD COLUMN IF NOT EXISTS min_order_cycle   INT          NULL COMMENT 'TUGE minOrderCycle' AFTER is_contract,
    ADD COLUMN IF NOT EXISTS first_order_price DECIMAL(28,2) NULL COMMENT 'TUGE firstOrderPrice 첫 주문가(USD)' AFTER min_order_cycle,
    ADD COLUMN IF NOT EXISTS raw_json          MEDIUMTEXT   NULL COMMENT 'TUGE 상품 원본 JSON (새 필드 대비)' AFTER first_order_price;
