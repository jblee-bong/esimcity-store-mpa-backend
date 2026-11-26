-- eSIM 충전 원가(결제 시점 환율 기준 실제 구매가, 원). 관리자 충전 목록 순익 계산용. 이 컬럼 추가 이후 충전분부터 값이 들어감
ALTER TABLE TB_TOPUP_ORDER
    ADD COLUMN topup_cost_krw INT NULL COMMENT '충전 원가(원)' AFTER topup_krw_price;
-- 충전 목록 날짜 기준. 이미 있으면 건너뜀 (없던 테이블이면 기존 행은 실행 시각으로 채워짐)
ALTER TABLE TB_TOPUP_ORDER
    ADD COLUMN IF NOT EXISTS regist_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
