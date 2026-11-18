-- 날짜 지정 상품(TUGE activeType=ACTIVEDBYORDER)의 사용 시작일(고객이 고른 한국 날짜). 그 외 상품은 NULL(장비가 처음 연결될 때 시작)
ALTER TABLE TB_WIFI_ORDER
    ADD COLUMN start_date DATE NULL COMMENT '사용 시작일(KST, 날짜 지정 상품만)' AFTER order_period;

-- TUGE 요금제 취소는 결제 환불과 따로 (관리자 [TUGE 취소] 버튼). 취소한 시각
ALTER TABLE TB_WIFI_ORDER
    ADD COLUMN tuge_cancel_dt DATETIME NULL COMMENT 'TUGE 주문 취소 시각' AFTER tuge_flow_order_nos;

-- 요금제 사용 기간·상태 (관리자 충전 주문 화면), 만료 하루 전 알림 발송 시각
ALTER TABLE TB_WIFI_ORDER
    ADD COLUMN plan_status VARCHAR(20) NULL COMMENT 'TUGE 요금제 상태 NOTACTIVE/INUSE/EXPIRED/GONE' AFTER tuge_cancel_dt,
    ADD COLUMN plan_start_dt VARCHAR(16) NULL COMMENT '사용 시작 (KST yyyy-MM-dd HH:mm)' AFTER plan_status,
    ADD COLUMN plan_end_dt VARCHAR(16) NULL COMMENT '사용 종료 (KST yyyy-MM-dd HH:mm)' AFTER plan_start_dt,
    ADD COLUMN expire_notified_dt DATETIME NULL COMMENT '종료 하루 전 알림 보낸 시각' AFTER plan_end_dt;
