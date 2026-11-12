-- 결제 적립 포인트 (실결제금액 x 적립률, 충전 완료 시)
ALTER TABLE TB_WIFI_ORDER
    ADD COLUMN earn_point INT NOT NULL DEFAULT 0 COMMENT '충전 완료 시 적립 포인트' AFTER pay_krw;

INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT 'EARN_RATE', 'WIFI', '', '결제 적립률(%)', '', 3, now(), now(), 'N', 'Y', '3' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = 'EARN_RATE');
