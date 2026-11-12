-- 나라별 가격정책 (1달러당 금액 + 마진율 + 일수별 마진율), 상품은 나라 1개에 소속
-- 먼저 실행: TB_WIFI_ALTER_20261112_period.sql, TB_WIFI_ALTER_20261112_category.sql

ALTER TABLE TB_WIFI_CATEGORY
    ADD COLUMN usd_rate     INT          NULL COMMENT '가격정책: 충전 1달러당 금액(원). 비우면 상위 지역 → 코드 WIFI RATE' AFTER use_yn,
    ADD COLUMN margin_rate  DECIMAL(6,2) NULL COMMENT '가격정책: 기본 마진율(%). 비우면 상위 지역 → 코드 WIFI MARGIN' AFTER usd_rate,
    ADD COLUMN period_rules VARCHAR(500) NULL COMMENT '가격정책: 일수별 마진율 [{"from":7,"rate":25}] (1일 데일리만)' AFTER margin_rate;

ALTER TABLE TB_WIFI_PRODUCT
    ADD COLUMN category_id BIGINT NULL COMMENT '나라 카테고리 (판매가는 이 나라 가격정책으로 계산)' AFTER sale_yn,
    ADD KEY idx_category_id (category_id);

-- 기존 다대다 연결 중 하나를 상품의 나라로 옮김
UPDATE TB_WIFI_PRODUCT p
   SET p.category_id = (SELECT MIN(pc.category_id) FROM TB_WIFI_PRODUCT_CATEGORY pc WHERE pc.product_code = p.product_code);

ALTER TABLE TB_WIFI_PRODUCT DROP COLUMN sale_krw;
DROP TABLE IF EXISTS TB_WIFI_PRODUCT_CATEGORY;

INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT 'MARGIN', 'WIFI', '', '기본 마진율(%)', '', 2, now(), now(), 'N', 'Y', '30' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = 'MARGIN');
UPDATE TB_CODE SET code_name = '기본 1달러당 금액(원)' WHERE code_group = 'WIFI' AND code_cd = 'RATE';
