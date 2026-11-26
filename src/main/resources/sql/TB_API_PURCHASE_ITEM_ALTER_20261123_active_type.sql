-- esimaccess 상품 개통 시작 기준(activeType). 1: 휴대폰에 설치하는 순간 시작, 2: 현지 통신망에 처음 연결할 때 시작
-- 이전에는 2(와 브라질·아르헨티나 단독)만 저장했는데, 이제 1도 저장하고 이 값으로 구분. 다른 API(TUGE 등) 상품은 NULL
ALTER TABLE TB_API_PURCHASE_ITEM
    ADD COLUMN IF NOT EXISTS api_purchase_active_type INT NULL COMMENT '개통 시작 기준 1:설치 시 2:첫 접속 시 (esimaccess)';
