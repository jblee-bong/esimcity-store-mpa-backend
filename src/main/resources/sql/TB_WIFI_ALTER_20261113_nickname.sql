-- 장비 별명 (없으면 화면에 SN 표시). 등록 해제하면 지움
ALTER TABLE TB_WIFI_DEVICE
    ADD COLUMN nickname VARCHAR(30) NULL COMMENT '고객이 붙인 별명' AFTER device_type;
