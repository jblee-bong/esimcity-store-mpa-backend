-- 다 쓰면: 비우면 TUGE data_limited 로 자동 (N=저속 계속, Y=추가 충전), 다르면 관리자가 SLOW / STOP 지정
ALTER TABLE TB_WIFI_PRODUCT
    ADD COLUMN after_use VARCHAR(10) NULL COMMENT '다 쓰면: SLOW=저속 계속, STOP=추가 충전, NULL=자동' AFTER data_unit;
