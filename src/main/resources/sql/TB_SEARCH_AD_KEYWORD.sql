-- 네이버 검색광고 파워링크 키워드 자동입찰 대상
-- DB에 직접 실행 후 사용

CREATE TABLE IF NOT EXISTS TB_SEARCH_AD_KEYWORD (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    ncc_keyword_id       VARCHAR(64)   NOT NULL COMMENT '네이버 검색광고 키워드 ID (nkw-...)',
    keyword              VARCHAR(200)  NOT NULL COMMENT '키워드 텍스트',
    use_yn               CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT 'Y: 자동입찰 대상',
    target_position      INT           NULL COMMENT '목표 순위 (NULL이면 전역 설정)',
    min_bid              INT           NULL COMMENT '최소 입찰가',
    max_bid              INT           NULL COMMENT '최대 입찰가',
    current_bid          INT           NULL COMMENT '최근 조회/반영된 실제 입찰가',
    estimate_bid         INT           NULL COMMENT '참고용 추정입찰가(미사용 가능)',
    avg_rnk              DECIMAL(10,2) NULL COMMENT '오늘 누적 평균노출순위',
    prev_imp_cnt         INT           NULL COMMENT '직전 스냅샷 누적 노출수',
    prev_avg_rnk         DECIMAL(10,2) NULL COMMENT '직전 스냅샷 누적 평균순위',
    last_imp_cnt         INT           NULL COMMENT '최근 조회 누적 노출수',
    delta_rnk            DECIMAL(10,2) NULL COMMENT '구간(약 1시간) 역산 평균순위',
    last_checked_dt      DATETIME      NULL COMMENT '입찰/통계 마지막 조회 시각',
    memo                 VARCHAR(500)  NULL,
    regist_dt            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_dt            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_yn               CHAR(1)       NOT NULL DEFAULT 'N',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ncc_keyword_id (ncc_keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='파워링크 키워드 자동입찰';

-- 기존 테이블 업그레이드:
-- ALTER TABLE TB_SEARCH_AD_KEYWORD
--   ADD COLUMN avg_rnk DECIMAL(10,2) NULL COMMENT '오늘 누적 평균노출순위' AFTER estimate_bid,
--   ADD COLUMN prev_imp_cnt INT NULL COMMENT '직전 스냅샷 누적 노출수' AFTER avg_rnk,
--   ADD COLUMN prev_avg_rnk DECIMAL(10,2) NULL COMMENT '직전 스냅샷 누적 평균순위' AFTER prev_imp_cnt,
--   ADD COLUMN last_imp_cnt INT NULL COMMENT '최근 조회 누적 노출수' AFTER prev_avg_rnk,
--   ADD COLUMN delta_rnk DECIMAL(10,2) NULL COMMENT '구간 역산 평균순위' AFTER last_imp_cnt;
