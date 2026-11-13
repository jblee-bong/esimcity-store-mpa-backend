-- 환불 신청(사용자) + 전액/부분 환불(관리자)
ALTER TABLE TB_WIFI_ORDER
    ADD COLUMN refund_status     VARCHAR(20)  NULL COMMENT 'REQUESTED/DONE/REJECTED' AFTER topup_status,
    ADD COLUMN refund_reason     VARCHAR(500) NULL COMMENT '사용자 환불 사유' AFTER refund_status,
    ADD COLUMN refund_request_dt DATETIME     NULL AFTER refund_reason,
    ADD COLUMN refund_krw        INT          NOT NULL DEFAULT 0 COMMENT '환불한 결제금액' AFTER refund_request_dt,
    ADD COLUMN refund_point      INT          NOT NULL DEFAULT 0 COMMENT '돌려준 사용 포인트' AFTER refund_krw,
    ADD COLUMN refund_memo       VARCHAR(500) NULL COMMENT '관리자 메모/거절 사유' AFTER refund_point,
    ADD COLUMN refund_dt         DATETIME     NULL AFTER refund_memo,
    ADD KEY idx_refund_status (refund_status);
