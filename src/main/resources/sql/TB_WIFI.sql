-- 글로벌 포켓 WiFi (TUGE T8) 충전 플랫폼

CREATE TABLE IF NOT EXISTS TB_WIFI_MEMBER (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    tel                VARCHAR(20)  NULL COMMENT '휴대폰번호(숫자만), 탈퇴시 NULL',
    kakao_id           VARCHAR(64)  NULL,
    status             VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'PRE(와디즈 선지급, 미가입)/ACTIVE/DELETED',
    point_balance      INT          NOT NULL DEFAULT 0,
    point_expire_dt    DATETIME     NULL COMMENT '포인트 소멸일',
    token_version      INT          NOT NULL DEFAULT 0 COMMENT '올리면 기존 로그인 무효',
    privacy_agree_dt   DATETIME     NULL,
    marketing_agree_yn CHAR(1)      NOT NULL DEFAULT 'N',
    regist_dt          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_dt      DATETIME     NULL,
    delete_dt          DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tel (tel),
    UNIQUE KEY uk_kakao_id (kakao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포켓와이파이 회원';

CREATE TABLE IF NOT EXISTS TB_WIFI_DEVICE (
    sn              VARCHAR(32)  NOT NULL COMMENT '장비번호(SN)',
    device_type     VARCHAR(20)  NULL COMMENT 'T8 등',
    tuge_status     VARCHAR(20)  NULL COMMENT 'ACTIVATED/NOTACTIVE/SUSPEND...',
    member_id       BIGINT       NULL COMMENT '등록된 회원',
    registered_dt   DATETIME     NULL,
    memo            VARCHAR(500) NULL,
    regist_dt       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_dt       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sn),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포켓와이파이 장비';

CREATE TABLE IF NOT EXISTS TB_WIFI_DEVICE_LOG (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    sn         VARCHAR(32) NOT NULL,
    member_id  BIGINT      NULL,
    action     VARCHAR(20) NOT NULL COMMENT 'REGISTER/RELEASE/ADMIN_RELEASE',
    admin_id   VARCHAR(100) NULL,
    regist_dt  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sn (sn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장비 등록/해제 이력';

CREATE TABLE IF NOT EXISTS TB_WIFI_PRODUCT (
    product_code        VARCHAR(100)  NOT NULL COMMENT 'TUGE productCode',
    tuge_name           VARCHAR(300)  NULL,
    product_type        VARCHAR(20)   NULL COMMENT 'DAILY/MONTH/MULTIPLEMONTHS',
    days                INT           NULL,
    validity_period     INT           NULL,
    data_limited        CHAR(1)       NULL,
    data_total          VARCHAR(20)   NULL,
    data_unit           VARCHAR(10)   NULL,
    daily_high_speed_mb INT           NULL COMMENT '데일리 하루 고속데이터(관리자 입력)',
    mcc                 VARCHAR(1000) NULL,
    device_type         VARCHAR(100)  NULL COMMENT '비어있으면 전 기종',
    settlement_usd      DECIMAL(28,2) NULL COMMENT '원가(USD)',
    popup_json          TEXT          NULL COMMENT '충전팩 목록 JSON',
    active_type         VARCHAR(30)   NULL,
    rule_desc           TEXT          NULL,
    tuge_modified_dt    VARCHAR(20)   NULL,
    tuge_active         CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT 'TUGE 판매중 여부',
    cost_changed_yn     CHAR(1)       NOT NULL DEFAULT 'N',
    sale_yn             CHAR(1)       NOT NULL DEFAULT 'N',
    category_id         BIGINT        NULL COMMENT '나라 카테고리 (판매가는 이 나라 가격정책으로 계산)',
    min_period          INT           NOT NULL DEFAULT 1 COMMENT '기간선택 상품 최소 일(월)수',
    max_period          INT           NOT NULL DEFAULT 30 COMMENT '기간선택 상품 최대 일(월)수',
    display_name        VARCHAR(200)  NULL,
    thumbnail           VARCHAR(500)  NULL,
    detail_html         MEDIUMTEXT    NULL,
    sort                INT           NOT NULL DEFAULT 0,
    regist_dt           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_dt           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_code),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포켓와이파이 상품(TUGE 동기화 + 판매설정)';

CREATE TABLE IF NOT EXISTS TB_WIFI_CATEGORY (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id BIGINT       NULL COMMENT '상위(지역) 카테고리. NULL 이면 지역, 값이 있으면 나라',
    name      VARCHAR(100) NOT NULL,
    image     VARCHAR(500) NULL,
    sort      INT          NOT NULL DEFAULT 0,
    use_yn    CHAR(1)      NOT NULL DEFAULT 'Y',
    usd_rate     INT          NULL COMMENT '가격정책: 충전 1달러당 금액(원). 비우면 상위 지역 → 코드 WIFI RATE',
    margin_rate  DECIMAL(6,2) NULL COMMENT '가격정책: 기본 마진율(%). 비우면 상위 지역 → 코드 WIFI MARGIN',
    period_rules VARCHAR(500) NULL COMMENT '가격정책: 일수별 마진율 [{"from":7,"rate":25}] (1일 데일리만)',
    regist_dt DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 카테고리 (지역 > 나라 2단계)';

CREATE TABLE IF NOT EXISTS TB_WIFI_ORDER (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    order_no            VARCHAR(40)   NOT NULL COMMENT 'WF...; PayUp orderNumber, TUGE otaOrderNo/requestId',
    member_id           BIGINT        NOT NULL,
    device_sn           VARCHAR(32)   NOT NULL,
    product_code        VARCHAR(100)  NOT NULL,
    option_id           BIGINT        NULL COMMENT '미사용(구 옵션 방식)',
    product_name        VARCHAR(300)  NULL COMMENT '주문시점 표시명',
    order_period        INT           NOT NULL DEFAULT 1,
    total_krw           INT           NOT NULL,
    point_used          INT           NOT NULL DEFAULT 0,
    pay_krw             INT           NOT NULL COMMENT 'PG 결제금액',
    earn_point          INT           NOT NULL DEFAULT 0 COMMENT '충전 완료 시 적립 포인트',
    settlement_usd      DECIMAL(28,2) NULL COMMENT '원가 스냅샷(USD x 기간)',
    payment_status      VARCHAR(20)   NOT NULL COMMENT 'PAY_READY/PAID/POINT_ONLY/PAY_FAILED/REFUNDED/REFUND_FAILED',
    topup_status        VARCHAR(20)   NOT NULL DEFAULT 'READY' COMMENT 'READY/RETRY/DONE/FAILED',
    pg_transaction_id   VARCHAR(100)  NULL,
    tuge_order_no       VARCHAR(100)  NULL,
    tuge_flow_order_nos VARCHAR(500)  NULL,
    retry_count         INT           NOT NULL DEFAULT 0,
    last_error          VARCHAR(1000) NULL,
    regist_dt           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_dt           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_member_id (member_id),
    KEY idx_device_sn (device_sn),
    KEY idx_status (payment_status, topup_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포켓와이파이 충전 주문';

CREATE TABLE IF NOT EXISTS TB_WIFI_POINT_LOG (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    member_id  BIGINT       NOT NULL,
    amount     INT          NOT NULL COMMENT '+지급/-사용',
    reason     VARCHAR(20)  NOT NULL COMMENT 'GRANT/USE/REFUND/EXPIRE/ADMIN/WITHDRAW',
    order_id   BIGINT       NULL,
    admin_id   VARCHAR(100) NULL,
    memo       VARCHAR(500) NULL,
    regist_dt  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='포인트 이력';

-- 가격정책 기본값 (나라/지역에 값이 없을 때): 코드관리 화면에서 수정
INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT '00', 'WIFI', '', '포켓와이파이 설정', '', 0, now(), now(), 'N', 'Y', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = '00');
INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT 'RATE', 'WIFI', '', 'USD 환율(원)', '', 1, now(), now(), 'N', 'Y', '1450' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = 'RATE');
INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT 'MARGIN', 'WIFI', '', '기본 마진율(%)', '', 2, now(), now(), 'N', 'Y', '30' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = 'MARGIN');
INSERT INTO TB_CODE (code_cd, code_group, code_prefix, code_name, code_sub_name, code_order, regist_dt, modify_dt, del_yn, use_yn, code_value)
SELECT 'EARN_RATE', 'WIFI', '', '결제 적립률(%)', '', 3, now(), now(), 'N', 'Y', '3' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM TB_CODE WHERE code_group = 'WIFI' AND code_cd = 'EARN_RATE');
