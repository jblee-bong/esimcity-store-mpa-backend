-- 쇼핑검색 소재 자동입찰 변경 이력
-- DB에 직접 실행 후 사용

CREATE TABLE IF NOT EXISTS TB_SEARCH_AD_SHOPPING_HIST (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    shopping_id      BIGINT        NOT NULL COMMENT 'TB_SEARCH_AD_SHOPPING.id',
    ncc_ad_id        VARCHAR(64)   NOT NULL COMMENT 'nad-...',
    ad_name          VARCHAR(200)  NULL COMMENT '소재명',
    action           VARCHAR(16)   NOT NULL COMMENT 'RAISE / LOWER',
    before_bid       INT           NULL COMMENT '변경 전 입찰가',
    after_bid        INT           NULL COMMENT '변경 후 입찰가',
    target_position  INT           NULL COMMENT '목표 순위',
    avg_rnk          DECIMAL(10,2) NULL COMMENT '오늘 누적 평균순위',
    delta_rnk        DECIMAL(10,2) NULL COMMENT '구간 역산 순위',
    last_imp_cnt     INT           NULL COMMENT '당시 누적 노출',
    delta_imp        INT           NULL COMMENT '구간 노출 증가분',
    regist_dt        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shopping_id_regist (shopping_id, regist_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='쇼핑검색 소재 입찰 변경 이력';
