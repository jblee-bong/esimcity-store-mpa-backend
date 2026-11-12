-- TB_PRODUCT(origin_product_no)별 마진(weight1~5).
-- exchange_weight 는 PRODUCT에 두지 않음 → 항상 TB_ESIM_PRICE(type) 사용 (EA 충전 포함)
-- 행이 없으면 origin_product_seller_management_code 앞 2글자(TG/TS/EA/WO...)로 TB_ESIM_PRICE 조회
CREATE TABLE IF NOT EXISTS TB_ESIM_PRICE_PRODUCT (
    origin_product_no     BIGINT       NOT NULL COMMENT 'TB_PRODUCT.origin_product_no',
    exchange_rate         DOUBLE       NULL COMMENT 'NULL이면 TB_ESIM_PRICE.exchange_rate',
    weight1               DOUBLE       NULL,
    weight2               DOUBLE       NULL,
    weight3               DOUBLE       NULL,
    weight4               DOUBLE       NULL,
    weight5               DOUBLE       NULL,
    PRIMARY KEY (origin_product_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='eSIM TB_PRODUCT별 마진';

-- 이미 만든 테이블이면:
-- ALTER TABLE TB_ESIM_PRICE_PRODUCT DROP COLUMN exchange_weight;
