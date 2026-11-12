-- eSIM 설치 안내 랜딩용 토큰 (알림톡 버튼 URL)
CREATE TABLE IF NOT EXISTS TB_ESIM_INSTALL_TOKEN (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    token        VARCHAR(64)   NOT NULL COMMENT 'URL 토큰',
    order_id     BIGINT        NULL COMMENT '주문 ID',
    payload_json MEDIUMTEXT    NOT NULL COMMENT '설치 안내 JSON',
    expire_dt    DATETIME      NOT NULL COMMENT '만료 시각',
    regist_dt    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_order_id (order_id),
    KEY idx_expire_dt (expire_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='eSIM 설치 안내 토큰';
