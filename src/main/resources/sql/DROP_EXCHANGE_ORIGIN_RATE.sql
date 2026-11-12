-- exchange_origin_rate 폐기: exchange_rate 하나만 사용
-- (적용 전 exchange_rate/exchange_weight 값을 새 기준으로 맞춘 뒤 실행)

ALTER TABLE TB_ESIM_PRICE DROP COLUMN exchange_origin_rate;

-- TB_ESIM_PRICE_PRODUCT 를 이미 만든 경우에만
-- ALTER TABLE TB_ESIM_PRICE_PRODUCT DROP COLUMN exchange_origin_rate;
