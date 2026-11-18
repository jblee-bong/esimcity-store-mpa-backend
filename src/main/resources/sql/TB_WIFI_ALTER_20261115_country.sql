-- 나라 직접 지정 (괌·사이판처럼 MCC 로 구분이 안 되는 상품). 비우면 TUGE MCC 로 자동
ALTER TABLE TB_WIFI_PRODUCT
    ADD COLUMN country_override VARCHAR(500) NULL COMMENT 'ISO 코드 콤마 (예: GU,MP). 비우면 MCC 자동' AFTER mcc;
