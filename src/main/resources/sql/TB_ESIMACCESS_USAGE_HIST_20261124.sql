-- esimaccess 데일리 상품의 "오늘 사용량" 계산용. API 는 누적 사용량만 주고 리셋은 activateTime + 24시간 × N (esimaccess 확인)
-- 1시간마다 /esim/usage/query 누적값을 쌓고, 오늘 사용량 = 현재 누적 - 이번 주기 시작 직전 누적
CREATE TABLE IF NOT EXISTS TB_ESIMACCESS_USAGE_HIST (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    esim_tran_no     VARCHAR(50) NOT NULL COMMENT 'esimaccess eSIM 번호',
    order_id         BIGINT      NULL COMMENT 'TB_ORDER.id',
    data_usage       BIGINT      NOT NULL COMMENT '누적 사용량(bytes)',
    total_data       BIGINT      NULL COMMENT '전체 용량(bytes)',
    last_update_time DATETIME    NOT NULL COMMENT 'esimaccess 정산 시각(UTC, 1시간 단위)',
    regist_dt        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tran_time (esim_tran_no, last_update_time)
) COMMENT 'esimaccess 누적 사용량 이력 (데일리 오늘 사용량 계산)';

-- 끝난 eSIM(만료·취소·회수). 하루 한 번 esimaccess 상태를 보고 넣고, 사용량 수집 대상에서 뺌
CREATE TABLE IF NOT EXISTS TB_ESIMACCESS_USAGE_DONE (
    esim_tran_no VARCHAR(50) NOT NULL PRIMARY KEY COMMENT 'esimaccess eSIM 번호',
    esim_status  VARCHAR(30) NULL COMMENT '끝난 상태 USED_EXPIRED / UNUSED_EXPIRED / CANCEL / REVOKED',
    regist_dt    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT 'esimaccess 사용량 수집 끝난 eSIM';
