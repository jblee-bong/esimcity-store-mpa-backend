-- 포켓와이파이 카테고리: 지역 → 나라 (2026-11-24 TUGE 운영 상품 674개 기준, 자동 생성)
-- 한 나라 상품 → 나라 카테고리 / 2개 나라 이상(일본·한국, 유럽 36개국 등) → 그 지역의 '패키지' 카테고리
-- 가격정책: 지역에 마진 40%, 데일리 일수별 마진 3일+ 35% / 7일+ 30% / 15일+ 25% (나라는 비워서 지역 값 상속). 1달러당 금액은 코드 WIFI RATE
-- 다시 돌려도 되게: 기존 카테고리·연결을 지우고 새로 만듦 (판매 여부 sale_yn 은 그대로)
UPDATE TB_WIFI_PRODUCT SET category_id = NULL;
DELETE FROM TB_WIFI_CATEGORY;

-- 동아시아
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '동아시아', 10, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '일본', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-006-AU-D-D-Glo-2GBH','A-006-AU-D-D-Glo-10GBH','A-006-T-AU-Glo-30D/D-3GB','A-006-T-AU-Glo-30D/D-8GB','A-006-T-AU-Glo-30D/D-15GB','A-006-T-AU-Glo-30D/D-30GB','A-006-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '대한민국', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-005-AU-D-D-Glo-2GBH','A-005-AU-D-D-Glo-10GBH','A-005-T-AU-Glo-30D/D-3GB','A-005-T-AU-Glo-30D/D-8GB','A-005-T-AU-Glo-30D/D-15GB','A-005-T-AU-Glo-30D/D-30GB','A-005-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '중국', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-003-VPN-AU-D-D-Glo-2GBH','A-003-VPN-AU-D-D-Glo-10GBH','A-003-VPN-T-AU-Glo-30D/D-3GB','A-003-VPN-T-AU-Glo-30D/D-8GB','A-003-VPN-T-AU-Glo-30D/D-15GB','A-003-VPN-T-AU-Glo-30D/D-30GB','A-003-VPN-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '홍콩', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-001-AU-D-D-Glo-2GBH','A-001-AU-D-D-Glo-10GBH','A-001-T-AU-Glo-30D/D-3GB','A-001-T-AU-Glo-30D/D-8GB','A-001-T-AU-Glo-30D/D-15GB','A-001-T-AU-Glo-30D/D-30GB','A-001-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '마카오', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-002-AU-D-D-Glo-2GBH','A-002-AU-D-D-Glo-10GBH','A-002-T-AU-Glo-30D/D-3GB','A-002-T-AU-Glo-30D/D-8GB','A-002-T-AU-Glo-30D/D-15GB','A-002-T-AU-Glo-30D/D-30GB','A-002-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '대만', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-004-AU-D-D-Glo-2GBH','A-004-AU-D-D-Glo-10GBH','A-004-T-AU-Glo-30D/D-3GB','A-004-T-AU-Glo-30D/D-8GB','A-004-T-AU-Glo-30D/D-15GB','A-004-T-AU-Glo-30D/D-30GB','A-004-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '몽골', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-033-AU-D-D-Glo-1GBH','A-033-AU-D-D-Glo-500MBH','A-033-T-AU-Glo-30D/D-3GB','A-033-T-AU-Glo-30D/D-8GB','A-033-T-AU-Glo-30D/D-15GB','A-033-T-AU-Glo-30D/D-30GB','A-033-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-136-AU-D-D-Glo-2GBH','A-136-AU-D-D-Glo-10GBH','A-136-T-AU-Glo-30D/D-8GB','A-136-T-AU-Glo-30D/D-15GB','A-136-T-AU-Glo-30D/D-30GB','A-136-T-AU-Glo-365D/D-100GB','A-101-AU-D-D-Glo-2GBH','A-101-AU-D-D-Glo-10GBH','A-101-T-AU-Glo-30D/D-8GB','A-101-T-AU-Glo-30D/D-15GB','A-101-T-AU-Glo-30D/D-30GB','A-101-T-AU-Glo-365D/D-100GB');

-- 동남아시아
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '동남아시아', 20, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '태국', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-007-AU-D-D-Glo-2GBH','A-007-AU-D-D-Glo-10GBH','A-007-T-AU-Glo-30D/D-3GB','A-007-T-AU-Glo-30D/D-8GB','A-007-T-AU-Glo-30D/D-15GB','A-007-T-AU-Glo-30D/D-30GB','A-007-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '베트남', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-008-AU-D-D-Glo-2GBH','A-008-AU-D-D-Glo-10GBH','A-008-T-AU-Glo-30D/D-3GB','A-008-T-AU-Glo-30D/D-8GB','A-008-T-AU-Glo-30D/D-15GB','A-008-T-AU-Glo-30D/D-30GB','A-008-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '필리핀', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-009-AU-D-D-Glo-2GBH','A-009-AU-D-D-Glo-10GBH','A-009-T-AU-Glo-30D/D-3GB','A-009-T-AU-Glo-30D/D-8GB','A-009-T-AU-Glo-30D/D-15GB','A-009-T-AU-Glo-30D/D-30GB','A-009-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '싱가포르', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-019-AU-D-D-Glo-2GBH','A-019-AU-D-D-Glo-10GBH','A-019-T-AU-Glo-30D/D-3GB','A-019-T-AU-Glo-30D/D-8GB','A-019-T-AU-Glo-30D/D-15GB','A-019-T-AU-Glo-30D/D-30GB','A-019-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '말레이시아', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-011-AU-D-D-Glo-2GBH','A-011-AU-D-D-Glo-10GBH','A-011-T-AU-Glo-30D/D-3GB','A-011-T-AU-Glo-30D/D-8GB','A-011-T-AU-Glo-30D/D-15GB','A-011-T-AU-Glo-30D/D-30GB','A-011-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '인도네시아', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-013-AU-D-D-Glo-2GBH','A-013-AU-D-D-Glo-10GBH','A-013-T-AU-Glo-30D/D-3GB','A-013-T-AU-Glo-30D/D-8GB','A-013-T-AU-Glo-30D/D-15GB','A-013-T-AU-Glo-30D/D-30GB','A-013-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '캄보디아', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-010-AU-D-D-Glo-2GBH','A-010-AU-D-D-Glo-500MBH','A-010-T-AU-Glo-30D/D-3GB','A-010-T-AU-Glo-30D/D-8GB','A-010-T-AU-Glo-30D/D-15GB','A-010-T-AU-Glo-30D/D-30GB','A-010-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '라오스', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-027-AU-D-D-Glo-2GBH','A-027-AU-D-D-Glo-500MBH','A-027-T-AU-Glo-30D/D-3GB','A-027-T-AU-Glo-30D/D-8GB','A-027-T-AU-Glo-30D/D-15GB','A-027-T-AU-Glo-30D/D-30GB','A-027-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '브루나이', 90, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-028-AU-D-D-Glo-1GBH','A-028-AU-D-D-Glo-500MBH','A-028-T-AU-Glo-30D/D-3GB','A-028-T-AU-Glo-30D/D-8GB','A-028-T-AU-Glo-30D/D-15GB','A-028-T-AU-Glo-30D/D-30GB','A-028-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 100, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-164-AU-D-D-Glo-2GBH','A-164-AU-D-D-Glo-10GBH','A-164-T-AU-Glo-30D/D-8GB','A-164-T-AU-Glo-30D/D-15GB','A-164-T-AU-Glo-30D/D-30GB','A-164-T-AU-Glo-365D/D-100GB');

-- 괌·사이판·오세아니아
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '괌·사이판·오세아니아', 30, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '괌', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('C-004-AU-D-D-Glo-2GBH','C-004-AU-D-D-Glo-500MBH','C-004-T-AU-Glo-30D/D-3GB','C-004-T-AU-Glo-30D/D-8GB','C-004-T-AU-Glo-30D/D-15GB','C-004-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '사이판·북마리아나', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('C-005-AU-D-D-Glo-2GBH','C-005-AU-D-D-Glo-500MBH','C-005-T-AU-Glo-30D/D-3GB','C-005-T-AU-Glo-30D/D-8GB','C-005-T-AU-Glo-30D/D-15GB','C-005-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '호주', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('C-001-AU-D-D-Glo-2GBH','C-001-AU-D-D-Glo-10GBH','C-001-T-AU-Glo-30D/D-3GB','C-001-T-AU-Glo-30D/D-8GB','C-001-T-AU-Glo-30D/D-15GB','C-001-T-AU-Glo-30D/D-30GB','C-001-T-AU-Glo-30D/D-100GB','C-001-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '뉴질랜드', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('C-002-AU-D-D-Glo-2GBH','C-002-AU-D-D-Glo-10GBH','C-002-T-AU-Glo-30D/D-3GB','C-002-T-AU-Glo-30D/D-8GB','C-002-T-AU-Glo-30D/D-15GB','C-002-T-AU-Glo-30D/D-30GB','C-002-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('C-101-AU-D-D-Glo-2GBH','C-101-AU-D-D-Glo-10GBH','C-101-T-AU-Glo-30D/D-8GB','C-101-T-AU-Glo-30D/D-15GB','C-101-T-AU-Glo-30D/D-30GB','C-103-AU-D-D-Glo-2GBH','C-103-AU-D-D-Glo-500MBH','C-103-T-AU-Glo-30D/D-3GB','C-103-T-AU-Glo-30D/D-8GB','C-103-T-AU-Glo-30D/D-15GB','C-103-T-AU-Glo-30D/D-30GB');

-- 남아시아
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '남아시아', 40, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '인도', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-016-AU-D-D-Glo-2GBH','A-016-AU-D-D-Glo-500MBH','A-016-T-AU-Glo-30D/D-3GB','A-016-T-AU-Glo-30D/D-8GB','A-016-T-AU-Glo-30D/D-15GB','A-016-T-AU-Glo-30D/D-30GB','A-016-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '스리랑카', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-014-AU-D-D-Glo-2GBH','A-014-AU-D-D-Glo-500MBH','A-014-T-AU-Glo-30D/D-3GB','A-014-T-AU-Glo-30D/D-8GB','A-014-T-AU-Glo-30D/D-15GB','A-014-T-AU-Glo-30D/D-30GB','A-014-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '네팔', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-032-AU-D-D-Glo-2GBH','A-032-AU-D-D-Glo-500MBH','A-032-T-AU-Glo-30D/D-3GB','A-032-T-AU-Glo-30D/D-8GB','A-032-T-AU-Glo-30D/D-15GB','A-032-T-AU-Glo-30D/D-30GB','A-032-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '방글라데시', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-030-AU-D-D-Glo-2GBH','A-030-AU-D-D-Glo-500MBH','A-030-T-AU-Glo-30D/D-3GB','A-030-T-AU-Glo-30D/D-8GB','A-030-T-AU-Glo-30D/D-15GB','A-030-T-AU-Glo-30D/D-30GB','A-030-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '파키스탄', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-031-AU-D-D-Glo-2GBH','A-031-AU-D-D-Glo-500MBH','A-031-T-AU-Glo-30D/D-3GB','A-031-T-AU-Glo-30D/D-8GB','A-031-T-AU-Glo-30D/D-15GB','A-031-T-AU-Glo-30D/D-30GB','A-031-T-AU-Glo-365D/D-100GB');

-- 중앙아시아·코카서스
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '중앙아시아·코카서스', 50, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '카자흐스탄', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-009-AU-D-D-Glo-1GBH','G-009-AU-D-D-Glo-500MBH','G-009-T-AU-Glo-30D/D-3GB','G-009-T-AU-Glo-30D/D-8GB','G-009-T-AU-Glo-30D/D-15GB','G-009-T-AU-Glo-30D/D-30GB','G-009-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '우즈베키스탄', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-011-AU-D-D-Glo-1GBH','G-011-AU-D-D-Glo-500MBH','G-011-T-AU-Glo-30D/D-3GB','G-011-T-AU-Glo-30D/D-8GB','G-011-T-AU-Glo-30D/D-15GB','G-011-T-AU-Glo-30D/D-30GB','G-011-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '키르기스스탄', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-035-AU-D-D-Glo-1GBH','A-035-AU-D-D-Glo-500MBH','A-035-T-AU-Glo-30D/D-3GB','A-035-T-AU-Glo-30D/D-8GB','A-035-T-AU-Glo-30D/D-15GB','A-035-T-AU-Glo-30D/D-30GB','A-035-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '조지아', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-054-AU-D-D-Glo-1GBH','E-054-AU-D-D-Glo-500MBH','E-054-T-AU-Glo-30D/D-3GB','E-054-T-AU-Glo-30D/D-8GB','E-054-T-AU-Glo-30D/D-15GB','E-054-T-AU-Glo-30D/D-30GB','E-054-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '아르메니아', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-056-AU-D-D-Glo-1GBH','E-056-AU-D-D-Glo-500MBH','E-056-T-AU-Glo-30D/D-3GB','E-056-T-AU-Glo-30D/D-8GB','E-056-T-AU-Glo-30D/D-15GB','E-056-T-AU-Glo-30D/D-30GB','E-056-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '아제르바이잔', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-010-AU-D-D-Glo-1GBH','G-010-AU-D-D-Glo-500MBH','G-010-T-AU-Glo-30D/D-3GB','G-010-T-AU-Glo-30D/D-8GB','G-010-T-AU-Glo-30D/D-15GB','G-010-T-AU-Glo-30D/D-30GB','G-010-T-AU-Glo-365D/D-100GB');

-- 중동
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '중동', 60, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '아랍에미리트', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-001-AU-D-D-Glo-1GBH','G-001-AU-D-D-Glo-500MBH','G-001-T-AU-Glo-30D/D-3GB','G-001-T-AU-Glo-30D/D-8GB','G-001-T-AU-Glo-30D/D-15GB','G-001-T-AU-Glo-30D/D-30GB','G-001-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '이스라엘', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-002-AU-D-D-Glo-2GBH','G-002-AU-D-D-Glo-500MBH','G-002-T-AU-Glo-30D/D-3GB','G-002-T-AU-Glo-30D/D-8GB','G-002-T-AU-Glo-30D/D-15GB','G-002-T-AU-Glo-30D/D-30GB','G-002-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '사우디아라비아', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-003-AU-D-D-Glo-1GBH','G-003-AU-D-D-Glo-500MBH','G-003-T-AU-Glo-30D/D-3GB','G-003-T-AU-Glo-30D/D-8GB','G-003-T-AU-Glo-30D/D-15GB','G-003-T-AU-Glo-30D/D-30GB','G-003-T-AU-Glo-30D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '카타르', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-004-AU-D-D-Glo-1GBH','G-004-AU-D-D-Glo-500MBH','G-004-T-AU-Glo-30D/D-3GB','G-004-T-AU-Glo-30D/D-8GB','G-004-T-AU-Glo-30D/D-15GB','G-004-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '요르단', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-005-AU-D-D-Glo-1GBH','G-005-AU-D-D-Glo-500MBH','G-005-T-AU-Glo-30D/D-3GB','G-005-T-AU-Glo-30D/D-8GB','G-005-T-AU-Glo-30D/D-15GB','G-005-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '쿠웨이트', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-006-AU-D-D-Glo-1GBH','G-006-AU-D-D-Glo-500MBH','G-006-T-AU-Glo-30D/D-3GB','G-006-T-AU-Glo-30D/D-8GB','G-006-T-AU-Glo-30D/D-15GB','G-006-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '바레인', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-007-AU-D-D-Glo-1GBH','G-007-AU-D-D-Glo-500MBH','G-007-T-AU-Glo-30D/D-3GB','G-007-T-AU-Glo-30D/D-8GB','G-007-T-AU-Glo-30D/D-15GB','G-007-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '오만', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-008-AU-D-D-Glo-1GBH','G-008-AU-D-D-Glo-500MBH','G-008-T-AU-Glo-30D/D-3GB','G-008-T-AU-Glo-30D/D-8GB','G-008-T-AU-Glo-30D/D-15GB','G-008-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '이라크', 90, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('A-034-AU-D-D-Glo-1GBH','A-034-AU-D-D-Glo-500MBH','A-034-T-AU-Glo-30D/D-3GB','A-034-T-AU-Glo-30D/D-8GB','A-034-T-AU-Glo-30D/D-15GB','A-034-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '이란', 100, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-013-AU-D-D-Glo-1GBH','G-013-AU-D-D-Glo-500MBH','G-013-T-AU-Glo-30D/D-3GB','G-013-T-AU-Glo-30D/D-8GB','G-013-T-AU-Glo-30D/D-15GB','G-013-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 110, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('G-106-AU-D-D-Glo-1GBH','G-106-AU-D-D-Glo-500MBH','G-106-T-AU-Glo-30D/D-8GB','G-106-T-AU-Glo-30D/D-15GB','G-106-T-AU-Glo-30D/D-30GB');

-- 유럽
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '유럽', 70, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '튀르키예', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-015-T-AU-Glo-30D/D-3GB','E-015-T-AU-Glo-30D/D-8GB','E-015-T-AU-Glo-30D/D-15GB','E-015-T-AU-Glo-30D/D-30GB','E-015-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '우크라이나', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-055-T-AU-Glo-30D/D-3GB','E-055-T-AU-Glo-30D/D-8GB','E-055-T-AU-Glo-30D/D-15GB','E-055-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '벨라루스', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-053-T-AU-Glo-30D/D-3GB','E-053-T-AU-Glo-30D/D-8GB','E-053-T-AU-Glo-30D/D-15GB','E-053-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '카나리아 제도', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-013-01-AU-D-D-Glo-1GBH','E-013-01-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-214-T-AU-Glo-30D/D-5GB','E-214-T-AU-Glo-30D/D-10GB','E-214-T-AU-Glo-30D/D-20GB','E-214-T-AU-Glo-30D/D-30GB','E-214-T-AU-Glo-30D/D-100GB','E-214-T-AU-Glo-365D/D-25GB','E-214-T-AU-Glo-365D/D-50GB','E-214-T-AU-Glo-365D/D-100GB','E-214-T-AU-Glo-730D/D-200GB','E-212-AU-D-D-Glo-2GBH','E-212-AU-D-D-Glo-10GBH','E-212-T-AU-Glo-30D/D-5GB','E-212-T-AU-Glo-30D/D-10GB','E-212-T-AU-Glo-30D/D-20GB','E-212-T-AU-Glo-30D/D-30GB','E-212-T-AU-Glo-30D/D-100GB','E-212-T-AU-Glo-30D/D-200GB','E-212-T-AU-Glo-30D/D-300GB','E-212-T-AU-Glo-365D/D-25GB','E-212-T-AU-Glo-365D/D-50GB','E-212-T-AU-Glo-365D/D-100GB','E-212-T-AU-Glo-365D/D-200GB','E-212-T-AU-Glo-365D/D-300GB','E-212-T-AU-Glo-730D/D-200GB','E-212-T-AU-Glo-730D/D-500GB','E-213-T-AU-Glo-30D/D-5GB','E-213-T-AU-Glo-30D/D-10GB','E-213-T-AU-Glo-30D/D-20GB','E-213-T-AU-Glo-30D/D-30GB','E-213-T-AU-Glo-365D/D-25GB','E-213-T-AU-Glo-365D/D-50GB');

-- 북미
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '북미', 80, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '미국', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('B-001-AU-D-D-Glo-2GBH','B-001-AU-D-D-Glo-10GBH','B-001-T-AU-Glo-30D/D-U','B-001-T-AU-Glo-30D/D-3GB','B-001-T-AU-Glo-30D/D-5GB','B-001-T-AU-Glo-30D/D-8GB','B-001-T-AU-Glo-30D/D-10GB','B-001-T-AU-Glo-30D/D-15GB','B-001-T-AU-Glo-30D/D-30GB','B-001-T-AU-Glo-30D/D-50GB','B-001-T-AU-Glo-30D/D-100GB','B-001-T-AU-Glo-365D/D-25GB','B-001-T-AU-Glo-365D/D-50GB','B-001-T-AU-Glo-365D/D-100GB','B-001-T-AU-Glo-365D/D-200GB','B-001-T-AU-Glo-365D/D-300GB','B-001-T-AU-Glo-730D/D-200GB','B-001-T-AU-Glo-730D/D-300GB','B-001-T-AU-Glo-730D/D-500GB','F-110-AU-D-D-Glo-1GBH','F-110-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '캐나다', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('B-002-AU-D-D-Glo-2GBH','B-002-AU-D-D-Glo-500MBH','B-002-T-AU-Glo-30D/D-3GB','B-002-T-AU-Glo-30D/D-8GB','B-002-T-AU-Glo-30D/D-15GB','B-002-T-AU-Glo-30D/D-30GB','B-002-T-AU-Glo-365D/D-25GB','B-002-T-AU-Glo-365D/D-50GB','B-002-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '멕시코', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('B-004-AU-D-D-Glo-2GBH','B-004-AU-D-D-Glo-500MBH','B-004-T-AU-Glo-30D/D-3GB','B-004-T-AU-Glo-30D/D-8GB','B-004-T-AU-Glo-30D/D-15GB','B-004-T-AU-Glo-30D/D-30GB','B-004-T-AU-Glo-365D/D-25GB','B-004-T-AU-Glo-365D/D-50GB','B-004-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('B-103-AU-D-D-Glo-2GBH','B-103-AU-D-D-Glo-500MBH','B-103-T-AU-Glo-30D/D-8GB','B-103-T-AU-Glo-30D/D-15GB','B-103-T-AU-Glo-30D/D-30GB');

-- 중남미
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '중남미', 90, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '브라질', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-001-AU-D-D-Glo-1GBH','F-001-AU-D-D-Glo-500MBH','F-001-T-AU-Glo-30D/D-3GB','F-001-T-AU-Glo-30D/D-8GB','F-001-T-AU-Glo-30D/D-15GB','F-001-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '아르헨티나', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-002-AU-D-D-Glo-1GBH','F-002-AU-D-D-Glo-500MBH','F-002-T-AU-Glo-30D/D-3GB','F-002-T-AU-Glo-30D/D-8GB','F-002-T-AU-Glo-30D/D-15GB','F-002-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '칠레', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-003-AU-D-D-Glo-1GBH','F-003-AU-D-D-Glo-500MBH','F-003-T-AU-Glo-30D/D-3GB','F-003-T-AU-Glo-30D/D-8GB','F-003-T-AU-Glo-30D/D-15GB','F-003-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '페루', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-008-AU-D-D-Glo-1GBH','F-008-AU-D-D-Glo-500MBH','F-008-T-AU-Glo-30D/D-3GB','F-008-T-AU-Glo-30D/D-8GB','F-008-T-AU-Glo-30D/D-15GB','F-008-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '콜롬비아', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-005-AU-D-D-Glo-1GBH','F-005-AU-D-D-Glo-500MBH','F-005-T-AU-Glo-30D/D-3GB','F-005-T-AU-Glo-30D/D-8GB','F-005-T-AU-Glo-30D/D-15GB','F-005-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '에콰도르', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-022-AU-D-D-Glo-1GBH','F-022-AU-D-D-Glo-500MBH','F-022-T-AU-Glo-30D/D-3GB','F-022-T-AU-Glo-30D/D-8GB','F-022-T-AU-Glo-30D/D-15GB','F-022-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '볼리비아', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-010-AU-D-D-Glo-1GBH','F-010-AU-D-D-Glo-500MBH','F-010-T-AU-Glo-30D/D-3GB','F-010-T-AU-Glo-30D/D-8GB','F-010-T-AU-Glo-30D/D-15GB','F-010-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '파라과이', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-024-AU-D-D-Glo-1GBH','F-024-AU-D-D-Glo-500MBH','F-024-T-AU-Glo-30D/D-3GB','F-024-T-AU-Glo-30D/D-8GB','F-024-T-AU-Glo-30D/D-15GB','F-024-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '우루과이', 90, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-019-AU-D-D-Glo-1GBH','F-019-AU-D-D-Glo-500MBH','F-019-T-AU-Glo-30D/D-3GB','F-019-T-AU-Glo-30D/D-8GB','F-019-T-AU-Glo-30D/D-15GB','F-019-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '베네수엘라', 100, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-006-AU-D-D-Glo-1GBH','F-006-AU-D-D-Glo-500MBH','F-006-T-AU-Glo-30D/D-3GB','F-006-T-AU-Glo-30D/D-8GB','F-006-T-AU-Glo-30D/D-15GB','F-006-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '가이아나', 110, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-017-AU-D-D-Glo-1GBH','F-017-AU-D-D-Glo-500MBH','F-017-T-AU-Glo-30D/D-3GB','F-017-T-AU-Glo-30D/D-8GB','F-017-T-AU-Glo-30D/D-15GB','F-017-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '수리남', 120, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-037-AU-D-D-Glo-1GBH','F-037-AU-D-D-Glo-500MBH','F-037-T-AU-Glo-30D/D-3GB','F-037-T-AU-Glo-30D/D-8GB','F-037-T-AU-Glo-30D/D-15GB','F-037-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '벨리즈', 130, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-025-AU-D-D-Glo-1GBH','F-025-AU-D-D-Glo-500MBH','F-025-T-AU-Glo-30D/D-3GB','F-025-T-AU-Glo-30D/D-8GB','F-025-T-AU-Glo-30D/D-15GB','F-025-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '과테말라', 140, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-016-AU-D-D-Glo-1GBH','F-016-AU-D-D-Glo-500MBH','F-016-T-AU-Glo-30D/D-3GB','F-016-T-AU-Glo-30D/D-8GB','F-016-T-AU-Glo-30D/D-15GB','F-016-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '엘살바도르', 150, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-015-AU-D-D-Glo-1GBH','F-015-AU-D-D-Glo-500MBH','F-015-T-AU-Glo-30D/D-3GB','F-015-T-AU-Glo-30D/D-8GB','F-015-T-AU-Glo-30D/D-15GB','F-015-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '온두라스', 160, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-023-AU-D-D-Glo-1GBH','F-023-AU-D-D-Glo-500MBH','F-023-T-AU-Glo-30D/D-3GB','F-023-T-AU-Glo-30D/D-8GB','F-023-T-AU-Glo-30D/D-15GB','F-023-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '니카라과', 170, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-021-AU-D-D-Glo-1GBH','F-021-AU-D-D-Glo-500MBH','F-021-T-AU-Glo-30D/D-3GB','F-021-T-AU-Glo-30D/D-8GB','F-021-T-AU-Glo-30D/D-15GB','F-021-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '코스타리카', 180, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-004-AU-D-D-Glo-1GBH','F-004-AU-D-D-Glo-500MBH','F-004-T-AU-Glo-30D/D-3GB','F-004-T-AU-Glo-30D/D-8GB','F-004-T-AU-Glo-30D/D-15GB','F-004-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '파나마', 190, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-007-AU-D-D-Glo-1GBH','F-007-AU-D-D-Glo-500MBH','F-007-T-AU-Glo-30D/D-3GB','F-007-T-AU-Glo-30D/D-8GB','F-007-T-AU-Glo-30D/D-15GB','F-007-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 200, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-110-T-AU-Glo-30D/D-8GB','F-110-T-AU-Glo-30D/D-15GB','F-110-T-AU-Glo-30D/D-30GB');

-- 카리브해
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '카리브해', 100, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '푸에르토리코', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-009-AU-D-D-Glo-1GBH','F-009-AU-D-D-Glo-500MBH','F-009-T-AU-Glo-30D/D-3GB','F-009-T-AU-Glo-30D/D-8GB','F-009-T-AU-Glo-30D/D-15GB','F-009-T-AU-Glo-30D/D-30GB','F-009-T-AU-Glo-365D/D-100GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '미국령 버진아일랜드', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-026-AU-D-D-Glo-1GBH','F-026-AU-D-D-Glo-500MBH','F-026-T-AU-Glo-30D/D-3GB','F-026-T-AU-Glo-30D/D-8GB','F-026-T-AU-Glo-30D/D-15GB','F-026-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '영국령 버진아일랜드', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-012-AU-D-D-Glo-1GBH','F-012-AU-D-D-Glo-500MBH','F-012-T-AU-Glo-30D/D-3GB','F-012-T-AU-Glo-30D/D-8GB','F-012-T-AU-Glo-30D/D-15GB','F-012-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '케이맨 제도', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-013-AU-D-D-Glo-1GBH','F-013-AU-D-D-Glo-500MBH','F-013-T-AU-Glo-30D/D-3GB','F-013-T-AU-Glo-30D/D-8GB','F-013-T-AU-Glo-30D/D-15GB','F-013-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '자메이카', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-018-AU-D-D-Glo-1GBH','F-018-AU-D-D-Glo-500MBH','F-018-T-AU-Glo-30D/D-3GB','F-018-T-AU-Glo-30D/D-8GB','F-018-T-AU-Glo-30D/D-15GB','F-018-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '바하마', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-011-AU-D-D-Glo-1GBH','F-011-AU-D-D-Glo-500MBH','F-011-T-AU-Glo-30D/D-3GB','F-011-T-AU-Glo-30D/D-8GB','F-011-T-AU-Glo-30D/D-15GB','F-011-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '도미니카 공화국', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-014-AU-D-D-Glo-1GBH','F-014-AU-D-D-Glo-500MBH','F-014-T-AU-Glo-30D/D-3GB','F-014-T-AU-Glo-30D/D-8GB','F-014-T-AU-Glo-30D/D-15GB','F-014-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '도미니카 연방', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-036-AU-D-D-Glo-1GBH','F-036-AU-D-D-Glo-500MBH','F-036-T-AU-Glo-30D/D-3GB','F-036-T-AU-Glo-30D/D-8GB','F-036-T-AU-Glo-30D/D-15GB','F-036-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '앤티가 바부다', 90, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-031-AU-D-D-Glo-1GBH','F-031-AU-D-D-Glo-500MBH','F-031-T-AU-Glo-30D/D-3GB','F-031-T-AU-Glo-30D/D-8GB','F-031-T-AU-Glo-30D/D-15GB','F-031-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '앵귈라', 100, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-029-AU-D-D-Glo-1GBH','F-029-AU-D-D-Glo-500MBH','F-029-T-AU-Glo-30D/D-3GB','F-029-T-AU-Glo-30D/D-8GB','F-029-T-AU-Glo-30D/D-15GB','F-029-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '바베이도스', 110, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-030-AU-D-D-Glo-1GBH','F-030-AU-D-D-Glo-500MBH','F-030-T-AU-Glo-30D/D-3GB','F-030-T-AU-Glo-30D/D-8GB','F-030-T-AU-Glo-30D/D-15GB','F-030-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '그레나다', 120, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-027-AU-D-D-Glo-1GBH','F-027-AU-D-D-Glo-500MBH','F-027-T-AU-Glo-30D/D-3GB','F-027-T-AU-Glo-30D/D-8GB','F-027-T-AU-Glo-30D/D-15GB','F-027-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '과들루프·마르티니크', 130, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-042-AU-D-D-Glo-1GBH','E-042-AU-D-D-Glo-500MBH','E-042-T-AU-Glo-30D/D-3GB','E-042-T-AU-Glo-30D/D-8GB','E-042-T-AU-Glo-30D/D-15GB','E-042-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '세인트키츠 네비스', 140, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-032-AU-D-D-Glo-1GBH','F-032-AU-D-D-Glo-500MBH','F-032-T-AU-Glo-30D/D-3GB','F-032-T-AU-Glo-30D/D-8GB','F-032-T-AU-Glo-30D/D-15GB','F-032-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '세인트루시아', 150, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-033-AU-D-D-Glo-1GBH','F-033-AU-D-D-Glo-500MBH','F-033-T-AU-Glo-30D/D-3GB','F-033-T-AU-Glo-30D/D-8GB','F-033-T-AU-Glo-30D/D-15GB','F-033-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '세인트빈센트 그레나딘', 160, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-034-AU-D-D-Glo-1GBH','F-034-AU-D-D-Glo-500MBH','F-034-T-AU-Glo-30D/D-3GB','F-034-T-AU-Glo-30D/D-8GB','F-034-T-AU-Glo-30D/D-15GB','F-034-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '터크스 케이커스 제도', 170, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('F-035-AU-D-D-Glo-1GBH','F-035-AU-D-D-Glo-500MBH','F-035-T-AU-Glo-30D/D-3GB','F-035-T-AU-Glo-30D/D-8GB','F-035-T-AU-Glo-30D/D-15GB','F-035-T-AU-Glo-30D/D-30GB');

-- 아프리카
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn, margin_rate, period_rules) VALUES (NULL, '아프리카', 110, 'Y', 40, '[{"from":3,"rate":35},{"from":7,"rate":30},{"from":15,"rate":25}]');
SET @r = LAST_INSERT_ID();
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '이집트', 10, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-002-AU-D-D-Glo-1GBH','D-002-AU-D-D-Glo-500MBH','D-002-T-AU-Glo-30D/D-8GB','D-002-T-AU-Glo-30D/D-15GB','D-002-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '모로코', 20, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-005-AU-D-D-Glo-1GBH','D-005-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '튀니지', 30, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-023-AU-D-D-Glo-1GBH','D-023-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '남아프리카공화국', 40, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-001-AU-D-D-Glo-1GBH','D-001-AU-D-D-Glo-500MBH','D-001-T-AU-Glo-30D/D-8GB','D-001-T-AU-Glo-30D/D-15GB','D-001-T-AU-Glo-30D/D-30GB');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '케냐', 50, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-006-AU-D-D-Glo-1GBH','D-006-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '탄자니아', 60, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-016-AU-D-D-Glo-1GBH','D-016-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '우간다', 70, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-017-AU-D-D-Glo-1GBH','D-017-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '르완다', 80, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-015-AU-D-D-Glo-1GBH','D-015-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '마다가스카르', 90, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-022-AU-D-D-Glo-1GBH','D-022-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '세이셸', 100, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-014-AU-D-D-Glo-1GBH','D-014-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '레위니옹', 110, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-021-AU-D-D-Glo-1GBH','D-021-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '마요트', 120, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('E-005-01-AU-D-D-Glo-1GBH','E-005-01-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '모잠비크', 130, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-012-AU-D-D-Glo-1GBH','D-012-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '말라위', 140, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-020-AU-D-D-Glo-1GBH','D-020-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '나이지리아', 150, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-007-AU-D-D-Glo-1GBH','D-007-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '가나', 160, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-011-AU-D-D-Glo-1GBH','D-011-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '코트디부아르', 170, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-018-AU-D-D-Glo-1GBH','D-018-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '가봉', 180, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-010-AU-D-D-Glo-1GBH','D-010-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '콩고민주공화국', 190, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-009-AU-D-D-Glo-1GBH','D-009-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '니제르', 200, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-013-AU-D-D-Glo-1GBH','D-013-AU-D-D-Glo-500MBH');
INSERT INTO TB_WIFI_CATEGORY (parent_id, name, sort, use_yn) VALUES (@r, '패키지', 210, 'Y');
UPDATE TB_WIFI_PRODUCT SET category_id = LAST_INSERT_ID() WHERE product_code IN ('D-107-AU-D-D-Glo-1GBH','D-107-AU-D-D-Glo-500MBH','D-107-T-AU-Glo-30D/D-8GB','D-107-T-AU-Glo-30D/D-15GB','D-107-T-AU-Glo-30D/D-30GB');

