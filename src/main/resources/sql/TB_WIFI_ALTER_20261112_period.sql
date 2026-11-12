-- TB_WIFI.sql 을 이미 실행한 DB 용: 가격 옵션 표 → 상품에 1일 가격 + 최소/최대 일수
ALTER TABLE TB_WIFI_PRODUCT
    ADD COLUMN sale_krw   INT NULL COMMENT '판매가: 기간선택 상품은 1일(1개월) 가격, 고정 상품은 상품 가격' AFTER sale_yn,
    ADD COLUMN min_period INT NOT NULL DEFAULT 1 COMMENT '기간선택 상품 최소 일(월)수' AFTER sale_krw,
    ADD COLUMN max_period INT NOT NULL DEFAULT 30 COMMENT '기간선택 상품 최대 일(월)수' AFTER min_period;

-- 기존 옵션 중 기간 1 가격을 상품 가격으로 옮김
UPDATE TB_WIFI_PRODUCT p
  JOIN TB_WIFI_PRODUCT_OPTION o ON o.product_code = p.product_code AND o.period = 1 AND o.use_yn = 'Y'
   SET p.sale_krw = o.sale_krw;

ALTER TABLE TB_WIFI_ORDER MODIFY option_id BIGINT NULL COMMENT '미사용(구 옵션 방식)';

DROP TABLE IF EXISTS TB_WIFI_PRODUCT_OPTION;
