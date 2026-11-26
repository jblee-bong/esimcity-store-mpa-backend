-- 운영 TUGE 상품 동기화 (2026-11-24)
-- 1) deviceType 이 620자(73기종)라 100자 칸에서 실패 → TUGE 에서 오는 글자 칸은 넉넉하게 / 목록류는 TEXT
-- 2) 문서에 없던 운영 응답 필드(highSpeed, limitSpeed, countryCodeList ...) 저장 + 원본 JSON 통째 보관(raw_json)
ALTER TABLE TB_WIFI_PRODUCT
    MODIFY tuge_name        VARCHAR(1000) NULL,
    MODIFY product_type     VARCHAR(50)   NULL COMMENT 'DAILY/MONTH/MULTIPLEMONTHS',
    MODIFY data_limited     VARCHAR(10)   NULL,
    MODIFY data_total       VARCHAR(50)   NULL,
    MODIFY data_unit        VARCHAR(20)   NULL,
    MODIFY mcc              TEXT          NULL,
    MODIFY device_type      TEXT          NULL COMMENT '비어있으면 전 기종',
    MODIFY active_type      VARCHAR(50)   NULL,
    MODIFY tuge_modified_dt VARCHAR(50)   NULL,
    ADD COLUMN IF NOT EXISTS high_speed        VARCHAR(50)  NULL COMMENT 'TUGE highSpeed 예: 2048(M) 하루 고속' AFTER rule_desc,
    ADD COLUMN IF NOT EXISTS limit_speed       VARCHAR(50)  NULL COMMENT 'TUGE limitSpeed 예: 2000(K) 소진 후 속도' AFTER high_speed,
    ADD COLUMN IF NOT EXISTS show_grade_number VARCHAR(200) NULL COMMENT 'TUGE showGradeNumber' AFTER limit_speed,
    ADD COLUMN IF NOT EXISTS country_code_list TEXT         NULL COMMENT 'TUGE countryCodeList (ISO)' AFTER show_grade_number,
    ADD COLUMN IF NOT EXISTS period_type       VARCHAR(50)  NULL COMMENT 'TUGE periodType BYDAY/BYMONTH' AFTER country_code_list,
    ADD COLUMN IF NOT EXISTS is_contract       VARCHAR(10)  NULL COMMENT 'TUGE isContract Y/N' AFTER period_type,
    ADD COLUMN IF NOT EXISTS min_order_cycle   INT          NULL COMMENT 'TUGE minOrderCycle' AFTER is_contract,
    ADD COLUMN IF NOT EXISTS first_order_price DECIMAL(28,2) NULL COMMENT 'TUGE firstOrderPrice 첫 주문가(USD)' AFTER min_order_cycle,
    ADD COLUMN IF NOT EXISTS raw_json          MEDIUMTEXT   NULL COMMENT 'TUGE 상품 원본 JSON (새 필드 대비)' AFTER first_order_price;
-- 포켓와이파이 MCC 나라 표 초기화 (2026-11-24 받은 MCC 목록으로 다시 채움)
-- mcc '-' 줄(MCC 없이 나라 이름만: 괌·사이판·아메리칸사모아 직접 지정용)은 남김
DELETE FROM TB_WIFI_MCC WHERE mcc <> '-';

INSERT IGNORE INTO TB_WIFI_MCC (mcc, iso, name) VALUES
('202', 'GR', '그리스'),
('204', 'NL', '네덜란드'),
('206', 'BE', '벨기에'),
('208', 'FR', '프랑스'),
('212', 'MC', '모나코'),
('213', 'AD', '안도라'),
('214', 'ES', '스페인'),
('216', 'HU', '헝가리'),
('218', 'BA', '보스니아 헤르체고비나'),
('219', 'HR', '크로아티아'),
('220', 'RS', '세르비아'),
('221', 'XK', '코소보'),
('222', 'IT', '이탈리아'),
('226', 'RO', '루마니아'),
('228', 'CH', '스위스'),
('230', 'CZ', '체코'),
('231', 'SK', '슬로바키아'),
('232', 'AT', '오스트리아'),
('234', 'GG', '건지'),
('234', 'IM', '맨섬'),
('234', 'GB', '영국'),
('235', 'GB', '영국'),
('238', 'DK', '덴마크'),
('240', 'SE', '스웨덴'),
('242', 'NO', '노르웨이'),
('244', 'FI', '핀란드'),
('246', 'LT', '리투아니아'),
('247', 'LV', '라트비아'),
('248', 'EE', '에스토니아'),
('250', 'RU', '러시아'),
('255', 'MD', '몰도바'),
('255', 'RU', '러시아'),
('255', 'UA', '우크라이나'),
('257', 'BY', '벨라루스'),
('259', 'MD', '몰도바'),
('260', 'PL', '폴란드'),
('262', 'DE', '독일'),
('266', 'GI', '지브롤터'),
('268', 'PT', '포르투갈'),
('270', 'LU', '룩셈부르크'),
('272', 'IE', '아일랜드'),
('274', 'IS', '아이슬란드'),
('276', 'AL', '알바니아'),
('278', 'MT', '몰타'),
('280', 'CY', '키프로스'),
('282', 'GE', '조지아'),
('283', 'AM', '아르메니아'),
('284', 'BG', '불가리아'),
('286', 'TR', '튀르키예'),
('288', 'FO', '페로 제도'),
('289', 'AB', '압하지야'),
('290', 'GL', '그린란드'),
('292', 'SM', '산마리노'),
('293', 'SI', '슬로베니아'),
('294', 'MK', '북마케도니아'),
('295', 'LI', '리히텐슈타인'),
('297', 'ME', '몬테네그로'),
('302', 'CA', '캐나다'),
('308', 'PM', '생피에르 미클롱'),
('310', 'BM', '버뮤다'),
('310', 'GU', '괌'),
('310', 'MP', '사이판·북마리아나'),
('310', 'PR', '푸에르토리코'),
('310', 'US', '미국'),
('311', 'GU', '괌'),
('311', 'US', '미국'),
('312', 'US', '미국'),
('313', 'PR', '푸에르토리코'),
('313', 'US', '미국'),
('314', 'US', '미국'),
('315', 'US', '미국'),
('316', 'US', '미국'),
('330', 'PR', '푸에르토리코'),
('334', 'MX', '멕시코'),
('338', 'BM', '버뮤다'),
('338', 'KY', '케이맨 제도'),
('338', 'JM', '자메이카'),
('340', 'GP', '과들루프·마르티니크'),
('342', 'BB', '바베이도스'),
('344', 'AG', '앤티가 바부다'),
('346', 'KY', '케이맨 제도'),
('348', 'VG', '영국령 버진아일랜드'),
('350', 'BM', '버뮤다'),
('352', 'GD', '그레나다'),
('354', 'MS', '몬트세랫'),
('356', 'KN', '세인트키츠 네비스'),
('358', 'LC', '세인트루시아'),
('360', 'VC', '세인트빈센트 그레나딘'),
('362', 'CW', '퀴라소'),
('363', 'AW', '아루바'),
('364', 'BS', '바하마'),
('365', 'AI', '앵귈라'),
('366', 'DM', '도미니카 연방'),
('368', 'CU', '쿠바'),
('370', 'DO', '도미니카 공화국'),
('372', 'HT', '아이티'),
('374', 'TT', '트리니다드 토바고'),
('376', 'TC', '터크스 케이커스 제도'),
('400', 'AZ', '아제르바이잔'),
('401', 'KZ', '카자흐스탄'),
('402', 'BT', '부탄'),
('404', 'IN', '인도'),
('405', 'IN', '인도'),
('410', 'PK', '파키스탄'),
('412', 'AF', '아프가니스탄'),
('413', 'LK', '스리랑카'),
('414', 'MM', '미얀마'),
('415', 'LB', '레바논'),
('416', 'JO', '요르단'),
('417', 'SY', '시리아'),
('418', 'IQ', '이라크'),
('419', 'KW', '쿠웨이트'),
('420', 'SA', '사우디아라비아'),
('421', 'YE', '예멘'),
('422', 'OM', '오만'),
('424', 'AE', '아랍에미리트'),
('425', 'IL', '이스라엘'),
('426', 'BH', '바레인'),
('427', 'QA', '카타르'),
('428', 'MN', '몽골'),
('429', 'NP', '네팔'),
('432', 'IR', '이란'),
('434', 'UZ', '우즈베키스탄'),
('436', 'TJ', '타지키스탄'),
('437', 'KG', '키르기스스탄'),
('438', 'TM', '투르크메니스탄'),
('440', 'JP', '일본'),
('441', 'JP', '일본'),
('450', 'KR', '대한민국'),
('452', 'VN', '베트남'),
('454', 'HK', '홍콩'),
('455', 'MO', '마카오'),
('456', 'KH', '캄보디아'),
('457', 'LA', '라오스'),
('460', 'CN', '중국'),
('466', 'TW', '대만'),
('467', 'KP', '북한'),
('470', 'BD', '방글라데시'),
('472', 'MV', '몰디브'),
('502', 'MY', '말레이시아'),
('505', 'AU', '호주'),
('510', 'ID', '인도네시아'),
('514', 'TL', '동티모르'),
('515', 'PH', '필리핀'),
('520', 'TH', '태국'),
('525', 'SG', '싱가포르'),
('528', 'BN', '브루나이'),
('530', 'NZ', '뉴질랜드'),
('536', 'NR', '나우루'),
('537', 'PG', '파푸아뉴기니'),
('539', 'TO', '통가'),
('540', 'SB', '솔로몬 제도'),
('541', 'VU', '바누아투'),
('542', 'FJ', '피지'),
('543', 'WF', '왈리스 푸투나'),
('544', 'AS', '아메리칸사모아'),
('545', 'KI', '키리바시'),
('546', 'NC', '뉴칼레도니아'),
('547', 'PF', '프랑스령 폴리네시아'),
('548', 'CK', '쿡 제도'),
('549', 'WS', '사모아'),
('550', 'FM', '미크로네시아'),
('551', 'MH', '마셜 제도'),
('552', 'PW', '팔라우'),
('553', 'TV', '투발루'),
('554', 'TK', '토켈라우'),
('555', 'NU', '니우에'),
('602', 'EG', '이집트'),
('603', 'DZ', '알제리'),
('604', 'MA', '모로코'),
('605', 'TN', '튀니지'),
('606', 'LY', '리비아'),
('607', 'GM', '감비아'),
('608', 'SN', '세네갈'),
('609', 'MR', '모리타니'),
('610', 'ML', '말리'),
('611', 'GN', '기니'),
('612', 'CI', '코트디부아르'),
('613', 'BF', '부르키나파소'),
('614', 'NE', '니제르'),
('615', 'TG', '토고'),
('616', 'BJ', '베냉'),
('617', 'MU', '모리셔스'),
('618', 'LR', '라이베리아'),
('619', 'SL', '시에라리온'),
('620', 'GH', '가나'),
('621', 'NG', '나이지리아'),
('622', 'TD', '차드'),
('623', 'CF', '중앙아프리카공화국'),
('624', 'CM', '카메룬'),
('625', 'CV', '카보베르데'),
('626', 'ST', '상투메 프린시페'),
('627', 'GQ', '적도기니'),
('628', 'GA', '가봉'),
('629', 'CG', '콩고'),
('630', 'CD', '콩고민주공화국'),
('631', 'AO', '앙골라'),
('632', 'GW', '기니비사우'),
('633', 'SC', '세이셸'),
('634', 'SD', '수단'),
('635', 'RW', '르완다'),
('636', 'ET', '에티오피아'),
('637', 'SO', '소말리아'),
('638', 'DJ', '지부티'),
('639', 'KE', '케냐'),
('640', 'TZ', '탄자니아'),
('641', 'UG', '우간다'),
('642', 'BI', '부룬디'),
('643', 'MZ', '모잠비크'),
('645', 'ZM', '잠비아'),
('646', 'MG', '마다가스카르'),
('647', 'RE', '레위니옹'),
('648', 'ZW', '짐바브웨'),
('649', 'NA', '나미비아'),
('650', 'MW', '말라위'),
('651', 'LS', '레소토'),
('652', 'BW', '보츠와나'),
('653', 'SZ', '에스와티니'),
('654', 'KM', '코모로'),
('655', 'ZA', '남아프리카공화국'),
('657', 'ER', '에리트레아'),
('658', 'SH', '세인트헬레나'),
('659', 'SS', '남수단'),
('702', 'BZ', '벨리즈'),
('704', 'GT', '과테말라'),
('706', 'SV', '엘살바도르'),
('708', 'HN', '온두라스'),
('710', 'NI', '니카라과'),
('712', 'CR', '코스타리카'),
('714', 'PA', '파나마'),
('716', 'PE', '페루'),
('722', 'AR', '아르헨티나'),
('724', 'BR', '브라질'),
('730', 'CL', '칠레'),
('732', 'CO', '콜롬비아'),
('734', 'VE', '베네수엘라'),
('736', 'BO', '볼리비아'),
('738', 'GY', '가이아나'),
('740', 'EC', '에콰도르'),
('742', 'GF', '프랑스령 기아나'),
('744', 'PY', '파라과이'),
('746', 'SR', '수리남'),
('748', 'UY', '우루과이'),
('750', 'FK', '포클랜드 제도');

-- TUGE countryCodeList 에는 나오는데 MCC 목록엔 없는 나라 (MCC 없이 이름만)
INSERT IGNORE INTO TB_WIFI_MCC (mcc, iso, name) VALUES
('-', 'VA', '바티칸'),
('-', 'VI', '미국령 버진아일랜드'),
('-', 'YT', '마요트'),
('-', 'IC', '카나리아 제도');  -- IC: ISO 예외 예약 코드 (스페인령)
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

-- 포켓와이파이 상품 표시명 (자동 생성, 2026-11-24). 규칙: [나라/패키지] [매일 n GB | n일 n GB]
-- 관리자가 이미 넣은 표시명은 안 덮어씀 (비어 있는 것만)
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 매일 1GB' WHERE product_code = 'G-001-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 매일 500MB' WHERE product_code = 'G-001-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 30일 3GB' WHERE product_code = 'G-001-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 30일 8GB' WHERE product_code = 'G-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 30일 15GB' WHERE product_code = 'G-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 30일 30GB' WHERE product_code = 'G-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아랍에미리트 365일 100GB' WHERE product_code = 'G-001-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 매일 1GB' WHERE product_code = 'F-031-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 매일 500MB' WHERE product_code = 'F-031-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 30일 3GB' WHERE product_code = 'F-031-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 30일 8GB' WHERE product_code = 'F-031-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 30일 15GB' WHERE product_code = 'F-031-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앤티가 바부다 30일 30GB' WHERE product_code = 'F-031-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 매일 1GB' WHERE product_code = 'F-029-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 매일 500MB' WHERE product_code = 'F-029-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 30일 3GB' WHERE product_code = 'F-029-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 30일 8GB' WHERE product_code = 'F-029-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 30일 15GB' WHERE product_code = 'F-029-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '앵귈라 30일 30GB' WHERE product_code = 'F-029-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 매일 1GB' WHERE product_code = 'E-056-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 매일 500MB' WHERE product_code = 'E-056-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 30일 3GB' WHERE product_code = 'E-056-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 30일 8GB' WHERE product_code = 'E-056-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 30일 15GB' WHERE product_code = 'E-056-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 30일 30GB' WHERE product_code = 'E-056-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르메니아 365일 100GB' WHERE product_code = 'E-056-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 매일 1GB' WHERE product_code = 'F-002-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 매일 500MB' WHERE product_code = 'F-002-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 30일 3GB' WHERE product_code = 'F-002-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 30일 8GB' WHERE product_code = 'F-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 30일 15GB' WHERE product_code = 'F-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아르헨티나 30일 30GB' WHERE product_code = 'F-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 매일 2GB' WHERE product_code = 'C-001-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 매일 10GB' WHERE product_code = 'C-001-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 30일 3GB' WHERE product_code = 'C-001-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 30일 8GB' WHERE product_code = 'C-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 30일 15GB' WHERE product_code = 'C-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 30일 30GB' WHERE product_code = 'C-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 30일 100GB' WHERE product_code = 'C-001-T-AU-Glo-30D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주 365일 100GB' WHERE product_code = 'C-001-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주·뉴질랜드 매일 2GB' WHERE product_code = 'C-101-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주·뉴질랜드 매일 10GB' WHERE product_code = 'C-101-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주·뉴질랜드 30일 8GB' WHERE product_code = 'C-101-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주·뉴질랜드 30일 15GB' WHERE product_code = 'C-101-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '호주·뉴질랜드 30일 30GB' WHERE product_code = 'C-101-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 매일 1GB' WHERE product_code = 'G-010-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 매일 500MB' WHERE product_code = 'G-010-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 30일 3GB' WHERE product_code = 'G-010-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 30일 8GB' WHERE product_code = 'G-010-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 30일 15GB' WHERE product_code = 'G-010-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 30일 30GB' WHERE product_code = 'G-010-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아제르바이잔 365일 100GB' WHERE product_code = 'G-010-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 매일 1GB' WHERE product_code = 'F-030-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 매일 500MB' WHERE product_code = 'F-030-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 30일 3GB' WHERE product_code = 'F-030-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 30일 8GB' WHERE product_code = 'F-030-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 30일 15GB' WHERE product_code = 'F-030-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바베이도스 30일 30GB' WHERE product_code = 'F-030-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 매일 2GB' WHERE product_code = 'A-030-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 매일 500MB' WHERE product_code = 'A-030-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 30일 3GB' WHERE product_code = 'A-030-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 30일 8GB' WHERE product_code = 'A-030-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 30일 15GB' WHERE product_code = 'A-030-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 30일 30GB' WHERE product_code = 'A-030-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '방글라데시 365일 100GB' WHERE product_code = 'A-030-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 30일 5GB' WHERE product_code = 'E-214-T-AU-Glo-30D/D-5GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 30일 10GB' WHERE product_code = 'E-214-T-AU-Glo-30D/D-10GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 30일 20GB' WHERE product_code = 'E-214-T-AU-Glo-30D/D-20GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 30일 30GB' WHERE product_code = 'E-214-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 30일 100GB' WHERE product_code = 'E-214-T-AU-Glo-30D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 365일 25GB' WHERE product_code = 'E-214-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 365일 50GB' WHERE product_code = 'E-214-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 365일 100GB' WHERE product_code = 'E-214-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '발칸 9개국 730일 200GB' WHERE product_code = 'E-214-T-AU-Glo-730D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 매일 1GB' WHERE product_code = 'G-007-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 매일 500MB' WHERE product_code = 'G-007-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 30일 3GB' WHERE product_code = 'G-007-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 30일 8GB' WHERE product_code = 'G-007-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 30일 15GB' WHERE product_code = 'G-007-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바레인 30일 30GB' WHERE product_code = 'G-007-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 매일 1GB' WHERE product_code = 'A-028-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 매일 500MB' WHERE product_code = 'A-028-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 30일 3GB' WHERE product_code = 'A-028-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 30일 8GB' WHERE product_code = 'A-028-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 30일 15GB' WHERE product_code = 'A-028-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 30일 30GB' WHERE product_code = 'A-028-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브루나이 365일 100GB' WHERE product_code = 'A-028-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 매일 1GB' WHERE product_code = 'F-010-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 매일 500MB' WHERE product_code = 'F-010-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 30일 3GB' WHERE product_code = 'F-010-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 30일 8GB' WHERE product_code = 'F-010-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 30일 15GB' WHERE product_code = 'F-010-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '볼리비아 30일 30GB' WHERE product_code = 'F-010-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 매일 1GB' WHERE product_code = 'F-001-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 매일 500MB' WHERE product_code = 'F-001-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 30일 3GB' WHERE product_code = 'F-001-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 30일 8GB' WHERE product_code = 'F-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 30일 15GB' WHERE product_code = 'F-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '브라질 30일 30GB' WHERE product_code = 'F-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 매일 1GB' WHERE product_code = 'F-011-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 매일 500MB' WHERE product_code = 'F-011-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 30일 3GB' WHERE product_code = 'F-011-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 30일 8GB' WHERE product_code = 'F-011-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 30일 15GB' WHERE product_code = 'F-011-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '바하마 30일 30GB' WHERE product_code = 'F-011-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨라루스 30일 3GB' WHERE product_code = 'E-053-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨라루스 30일 8GB' WHERE product_code = 'E-053-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨라루스 30일 15GB' WHERE product_code = 'E-053-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨라루스 30일 30GB' WHERE product_code = 'E-053-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 매일 1GB' WHERE product_code = 'F-025-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 매일 500MB' WHERE product_code = 'F-025-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 30일 3GB' WHERE product_code = 'F-025-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 30일 8GB' WHERE product_code = 'F-025-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 30일 15GB' WHERE product_code = 'F-025-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '벨리즈 30일 30GB' WHERE product_code = 'F-025-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 매일 2GB' WHERE product_code = 'B-002-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 매일 500MB' WHERE product_code = 'B-002-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 30일 3GB' WHERE product_code = 'B-002-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 30일 8GB' WHERE product_code = 'B-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 30일 15GB' WHERE product_code = 'B-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 30일 30GB' WHERE product_code = 'B-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 365일 25GB' WHERE product_code = 'B-002-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 365일 50GB' WHERE product_code = 'B-002-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캐나다 365일 100GB' WHERE product_code = 'B-002-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콩고민주공화국 매일 1GB' WHERE product_code = 'D-009-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콩고민주공화국 매일 500MB' WHERE product_code = 'D-009-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코트디부아르 매일 1GB' WHERE product_code = 'D-018-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코트디부아르 매일 500MB' WHERE product_code = 'D-018-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 매일 1GB' WHERE product_code = 'F-003-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 매일 500MB' WHERE product_code = 'F-003-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 30일 3GB' WHERE product_code = 'F-003-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 30일 8GB' WHERE product_code = 'F-003-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 30일 15GB' WHERE product_code = 'F-003-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '칠레 30일 30GB' WHERE product_code = 'F-003-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 매일 2GB' WHERE product_code = 'A-003-VPN-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 매일 10GB' WHERE product_code = 'A-003-VPN-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 30일 3GB' WHERE product_code = 'A-003-VPN-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 30일 8GB' WHERE product_code = 'A-003-VPN-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 30일 15GB' WHERE product_code = 'A-003-VPN-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 30일 30GB' WHERE product_code = 'A-003-VPN-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국 365일 100GB' WHERE product_code = 'A-003-VPN-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 매일 1GB' WHERE product_code = 'F-005-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 매일 500MB' WHERE product_code = 'F-005-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 30일 3GB' WHERE product_code = 'F-005-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 30일 8GB' WHERE product_code = 'F-005-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 30일 15GB' WHERE product_code = 'F-005-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '콜롬비아 30일 30GB' WHERE product_code = 'F-005-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 매일 1GB' WHERE product_code = 'F-004-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 매일 500MB' WHERE product_code = 'F-004-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 30일 3GB' WHERE product_code = 'F-004-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 30일 8GB' WHERE product_code = 'F-004-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 30일 15GB' WHERE product_code = 'F-004-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '코스타리카 30일 30GB' WHERE product_code = 'F-004-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 매일 2GB' WHERE product_code = 'E-212-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 매일 10GB' WHERE product_code = 'E-212-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 5GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-5GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 10GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-10GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 20GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-20GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 30GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 100GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 200GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 30일 300GB' WHERE product_code = 'E-212-T-AU-Glo-30D/D-300GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 365일 25GB' WHERE product_code = 'E-212-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 365일 50GB' WHERE product_code = 'E-212-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 365일 100GB' WHERE product_code = 'E-212-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 365일 200GB' WHERE product_code = 'E-212-T-AU-Glo-365D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 365일 300GB' WHERE product_code = 'E-212-T-AU-Glo-365D/D-300GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 730일 200GB' WHERE product_code = 'E-212-T-AU-Glo-730D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽 36개국 730일 500GB' WHERE product_code = 'E-212-T-AU-Glo-730D/D-500GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 30일 5GB' WHERE product_code = 'E-213-T-AU-Glo-30D/D-5GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 30일 10GB' WHERE product_code = 'E-213-T-AU-Glo-30D/D-10GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 30일 20GB' WHERE product_code = 'E-213-T-AU-Glo-30D/D-20GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 30일 30GB' WHERE product_code = 'E-213-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 365일 25GB' WHERE product_code = 'E-213-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '유럽·발칸 46개국 365일 50GB' WHERE product_code = 'E-213-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 매일 1GB' WHERE product_code = 'F-036-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 매일 500MB' WHERE product_code = 'F-036-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 30일 3GB' WHERE product_code = 'F-036-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 30일 8GB' WHERE product_code = 'F-036-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 30일 15GB' WHERE product_code = 'F-036-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 연방 30일 30GB' WHERE product_code = 'F-036-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 매일 1GB' WHERE product_code = 'F-014-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 매일 500MB' WHERE product_code = 'F-014-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 30일 3GB' WHERE product_code = 'F-014-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 30일 8GB' WHERE product_code = 'F-014-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 30일 15GB' WHERE product_code = 'F-014-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '도미니카 공화국 30일 30GB' WHERE product_code = 'F-014-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 매일 1GB' WHERE product_code = 'F-022-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 매일 500MB' WHERE product_code = 'F-022-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 30일 3GB' WHERE product_code = 'F-022-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 30일 8GB' WHERE product_code = 'F-022-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 30일 15GB' WHERE product_code = 'F-022-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '에콰도르 30일 30GB' WHERE product_code = 'F-022-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이집트 매일 1GB' WHERE product_code = 'D-002-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이집트 매일 500MB' WHERE product_code = 'D-002-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이집트 30일 8GB' WHERE product_code = 'D-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이집트 30일 15GB' WHERE product_code = 'D-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이집트 30일 30GB' WHERE product_code = 'D-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아프리카 19개국 매일 1GB' WHERE product_code = 'D-107-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아프리카 19개국 매일 500MB' WHERE product_code = 'D-107-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아프리카 19개국 30일 8GB' WHERE product_code = 'D-107-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아프리카 19개국 30일 15GB' WHERE product_code = 'D-107-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아프리카 19개국 30일 30GB' WHERE product_code = 'D-107-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카나리아 제도 매일 1GB' WHERE product_code = 'E-013-01-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카나리아 제도 매일 500MB' WHERE product_code = 'E-013-01-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마요트 매일 1GB' WHERE product_code = 'E-005-01-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마요트 매일 500MB' WHERE product_code = 'E-005-01-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가봉 매일 1GB' WHERE product_code = 'D-010-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가봉 매일 500MB' WHERE product_code = 'D-010-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 매일 1GB' WHERE product_code = 'F-027-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 매일 500MB' WHERE product_code = 'F-027-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 30일 3GB' WHERE product_code = 'F-027-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 30일 8GB' WHERE product_code = 'F-027-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 30일 15GB' WHERE product_code = 'F-027-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '그레나다 30일 30GB' WHERE product_code = 'F-027-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 매일 1GB' WHERE product_code = 'E-054-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 매일 500MB' WHERE product_code = 'E-054-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 30일 3GB' WHERE product_code = 'E-054-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 30일 8GB' WHERE product_code = 'E-054-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 30일 15GB' WHERE product_code = 'E-054-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 30일 30GB' WHERE product_code = 'E-054-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '조지아 365일 100GB' WHERE product_code = 'E-054-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가나 매일 1GB' WHERE product_code = 'D-011-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가나 매일 500MB' WHERE product_code = 'D-011-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 매일 1GB' WHERE product_code = 'E-042-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 매일 500MB' WHERE product_code = 'E-042-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 30일 3GB' WHERE product_code = 'E-042-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 30일 8GB' WHERE product_code = 'E-042-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 30일 15GB' WHERE product_code = 'E-042-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과들루프 30일 30GB' WHERE product_code = 'E-042-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 매일 1GB' WHERE product_code = 'F-016-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 매일 500MB' WHERE product_code = 'F-016-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 30일 3GB' WHERE product_code = 'F-016-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 30일 8GB' WHERE product_code = 'F-016-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 30일 15GB' WHERE product_code = 'F-016-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '과테말라 30일 30GB' WHERE product_code = 'F-016-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 매일 2GB' WHERE product_code = 'C-004-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 매일 500MB' WHERE product_code = 'C-004-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 30일 3GB' WHERE product_code = 'C-004-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 30일 8GB' WHERE product_code = 'C-004-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 30일 15GB' WHERE product_code = 'C-004-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌 30일 30GB' WHERE product_code = 'C-004-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 매일 1GB' WHERE product_code = 'F-017-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 매일 500MB' WHERE product_code = 'F-017-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 30일 3GB' WHERE product_code = 'F-017-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 30일 8GB' WHERE product_code = 'F-017-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 30일 15GB' WHERE product_code = 'F-017-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '가이아나 30일 30GB' WHERE product_code = 'F-017-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 매일 2GB' WHERE product_code = 'A-001-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 매일 10GB' WHERE product_code = 'A-001-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 30일 3GB' WHERE product_code = 'A-001-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 30일 8GB' WHERE product_code = 'A-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 30일 15GB' WHERE product_code = 'A-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 30일 30GB' WHERE product_code = 'A-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '홍콩 365일 100GB' WHERE product_code = 'A-001-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 매일 2GB' WHERE product_code = 'A-136-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 매일 10GB' WHERE product_code = 'A-136-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 30일 8GB' WHERE product_code = 'A-136-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 30일 15GB' WHERE product_code = 'A-136-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 30일 30GB' WHERE product_code = 'A-136-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중국·홍콩·마카오 365일 100GB' WHERE product_code = 'A-136-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 매일 1GB' WHERE product_code = 'F-023-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 매일 500MB' WHERE product_code = 'F-023-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 30일 3GB' WHERE product_code = 'F-023-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 30일 8GB' WHERE product_code = 'F-023-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 30일 15GB' WHERE product_code = 'F-023-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '온두라스 30일 30GB' WHERE product_code = 'F-023-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 매일 2GB' WHERE product_code = 'A-013-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 매일 10GB' WHERE product_code = 'A-013-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 30일 3GB' WHERE product_code = 'A-013-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 30일 8GB' WHERE product_code = 'A-013-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 30일 15GB' WHERE product_code = 'A-013-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 30일 30GB' WHERE product_code = 'A-013-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도네시아 365일 100GB' WHERE product_code = 'A-013-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 매일 2GB' WHERE product_code = 'G-002-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 매일 500MB' WHERE product_code = 'G-002-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 30일 3GB' WHERE product_code = 'G-002-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 30일 8GB' WHERE product_code = 'G-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 30일 15GB' WHERE product_code = 'G-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 30일 30GB' WHERE product_code = 'G-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이스라엘 365일 100GB' WHERE product_code = 'G-002-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중동 8개국 매일 1GB' WHERE product_code = 'G-106-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중동 8개국 매일 500MB' WHERE product_code = 'G-106-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중동 8개국 30일 8GB' WHERE product_code = 'G-106-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중동 8개국 30일 15GB' WHERE product_code = 'G-106-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '중동 8개국 30일 30GB' WHERE product_code = 'G-106-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 매일 2GB' WHERE product_code = 'A-016-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 매일 500MB' WHERE product_code = 'A-016-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 30일 3GB' WHERE product_code = 'A-016-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 30일 8GB' WHERE product_code = 'A-016-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 30일 15GB' WHERE product_code = 'A-016-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 30일 30GB' WHERE product_code = 'A-016-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '인도 365일 100GB' WHERE product_code = 'A-016-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 매일 1GB' WHERE product_code = 'A-034-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 매일 500MB' WHERE product_code = 'A-034-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 30일 3GB' WHERE product_code = 'A-034-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 30일 8GB' WHERE product_code = 'A-034-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 30일 15GB' WHERE product_code = 'A-034-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이라크 30일 30GB' WHERE product_code = 'A-034-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 매일 1GB' WHERE product_code = 'G-013-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 매일 500MB' WHERE product_code = 'G-013-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 30일 3GB' WHERE product_code = 'G-013-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 30일 8GB' WHERE product_code = 'G-013-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 30일 15GB' WHERE product_code = 'G-013-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '이란 30일 30GB' WHERE product_code = 'G-013-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 매일 1GB' WHERE product_code = 'F-018-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 매일 500MB' WHERE product_code = 'F-018-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 30일 3GB' WHERE product_code = 'F-018-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 30일 8GB' WHERE product_code = 'F-018-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 30일 15GB' WHERE product_code = 'F-018-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '자메이카 30일 30GB' WHERE product_code = 'F-018-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 매일 1GB' WHERE product_code = 'G-005-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 매일 500MB' WHERE product_code = 'G-005-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 30일 3GB' WHERE product_code = 'G-005-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 30일 8GB' WHERE product_code = 'G-005-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 30일 15GB' WHERE product_code = 'G-005-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '요르단 30일 30GB' WHERE product_code = 'G-005-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 매일 2GB' WHERE product_code = 'A-006-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 매일 10GB' WHERE product_code = 'A-006-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 30일 3GB' WHERE product_code = 'A-006-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 30일 8GB' WHERE product_code = 'A-006-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 30일 15GB' WHERE product_code = 'A-006-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 30일 30GB' WHERE product_code = 'A-006-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본 365일 100GB' WHERE product_code = 'A-006-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 매일 2GB' WHERE product_code = 'A-101-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 매일 10GB' WHERE product_code = 'A-101-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 30일 8GB' WHERE product_code = 'A-101-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 30일 15GB' WHERE product_code = 'A-101-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 30일 30GB' WHERE product_code = 'A-101-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '일본·한국 365일 100GB' WHERE product_code = 'A-101-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케냐 매일 1GB' WHERE product_code = 'D-006-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케냐 매일 500MB' WHERE product_code = 'D-006-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 매일 1GB' WHERE product_code = 'A-035-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 매일 500MB' WHERE product_code = 'A-035-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 30일 3GB' WHERE product_code = 'A-035-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 30일 8GB' WHERE product_code = 'A-035-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 30일 15GB' WHERE product_code = 'A-035-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 30일 30GB' WHERE product_code = 'A-035-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '키르기스스탄 365일 100GB' WHERE product_code = 'A-035-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 매일 2GB' WHERE product_code = 'A-010-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 매일 500MB' WHERE product_code = 'A-010-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 30일 3GB' WHERE product_code = 'A-010-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 30일 8GB' WHERE product_code = 'A-010-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 30일 15GB' WHERE product_code = 'A-010-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 30일 30GB' WHERE product_code = 'A-010-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '캄보디아 365일 100GB' WHERE product_code = 'A-010-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 매일 1GB' WHERE product_code = 'F-032-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 매일 500MB' WHERE product_code = 'F-032-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 30일 3GB' WHERE product_code = 'F-032-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 30일 8GB' WHERE product_code = 'F-032-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 30일 15GB' WHERE product_code = 'F-032-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트키츠 네비스 30일 30GB' WHERE product_code = 'F-032-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 매일 2GB' WHERE product_code = 'A-005-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 매일 10GB' WHERE product_code = 'A-005-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 30일 3GB' WHERE product_code = 'A-005-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 30일 8GB' WHERE product_code = 'A-005-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 30일 15GB' WHERE product_code = 'A-005-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 30일 30GB' WHERE product_code = 'A-005-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '한국 365일 100GB' WHERE product_code = 'A-005-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 매일 1GB' WHERE product_code = 'G-006-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 매일 500MB' WHERE product_code = 'G-006-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 30일 3GB' WHERE product_code = 'G-006-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 30일 8GB' WHERE product_code = 'G-006-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 30일 15GB' WHERE product_code = 'G-006-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '쿠웨이트 30일 30GB' WHERE product_code = 'G-006-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 매일 1GB' WHERE product_code = 'F-013-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 매일 500MB' WHERE product_code = 'F-013-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 30일 3GB' WHERE product_code = 'F-013-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 30일 8GB' WHERE product_code = 'F-013-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 30일 15GB' WHERE product_code = 'F-013-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '케이맨 제도 30일 30GB' WHERE product_code = 'F-013-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 매일 1GB' WHERE product_code = 'G-009-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 매일 500MB' WHERE product_code = 'G-009-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 30일 3GB' WHERE product_code = 'G-009-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 30일 8GB' WHERE product_code = 'G-009-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 30일 15GB' WHERE product_code = 'G-009-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 30일 30GB' WHERE product_code = 'G-009-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카자흐스탄 365일 100GB' WHERE product_code = 'G-009-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 매일 2GB' WHERE product_code = 'A-027-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 매일 500MB' WHERE product_code = 'A-027-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 30일 3GB' WHERE product_code = 'A-027-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 30일 8GB' WHERE product_code = 'A-027-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 30일 15GB' WHERE product_code = 'A-027-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 30일 30GB' WHERE product_code = 'A-027-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '라오스 365일 100GB' WHERE product_code = 'A-027-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 매일 1GB' WHERE product_code = 'F-033-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 매일 500MB' WHERE product_code = 'F-033-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 30일 3GB' WHERE product_code = 'F-033-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 30일 8GB' WHERE product_code = 'F-033-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 30일 15GB' WHERE product_code = 'F-033-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트루시아 30일 30GB' WHERE product_code = 'F-033-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 매일 2GB' WHERE product_code = 'A-014-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 매일 500MB' WHERE product_code = 'A-014-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 30일 3GB' WHERE product_code = 'A-014-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 30일 8GB' WHERE product_code = 'A-014-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 30일 15GB' WHERE product_code = 'A-014-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 30일 30GB' WHERE product_code = 'A-014-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '스리랑카 365일 100GB' WHERE product_code = 'A-014-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '모로코 매일 1GB' WHERE product_code = 'D-005-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '모로코 매일 500MB' WHERE product_code = 'D-005-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마다가스카르 매일 1GB' WHERE product_code = 'D-022-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마다가스카르 매일 500MB' WHERE product_code = 'D-022-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 매일 1GB' WHERE product_code = 'A-033-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 매일 500MB' WHERE product_code = 'A-033-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 30일 3GB' WHERE product_code = 'A-033-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 30일 8GB' WHERE product_code = 'A-033-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 30일 15GB' WHERE product_code = 'A-033-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 30일 30GB' WHERE product_code = 'A-033-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '몽골 365일 100GB' WHERE product_code = 'A-033-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 매일 2GB' WHERE product_code = 'A-002-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 매일 10GB' WHERE product_code = 'A-002-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 30일 3GB' WHERE product_code = 'A-002-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 30일 8GB' WHERE product_code = 'A-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 30일 15GB' WHERE product_code = 'A-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 30일 30GB' WHERE product_code = 'A-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '마카오 365일 100GB' WHERE product_code = 'A-002-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말라위 매일 1GB' WHERE product_code = 'D-020-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말라위 매일 500MB' WHERE product_code = 'D-020-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 매일 2GB' WHERE product_code = 'B-004-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 매일 500MB' WHERE product_code = 'B-004-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 30일 3GB' WHERE product_code = 'B-004-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 30일 8GB' WHERE product_code = 'B-004-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 30일 15GB' WHERE product_code = 'B-004-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 30일 30GB' WHERE product_code = 'B-004-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 365일 25GB' WHERE product_code = 'B-004-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 365일 50GB' WHERE product_code = 'B-004-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '멕시코 365일 100GB' WHERE product_code = 'B-004-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국·캐나다·멕시코 매일 2GB' WHERE product_code = 'B-103-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국·캐나다·멕시코 매일 500MB' WHERE product_code = 'B-103-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국·캐나다·멕시코 30일 8GB' WHERE product_code = 'B-103-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국·캐나다·멕시코 30일 15GB' WHERE product_code = 'B-103-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국·캐나다·멕시코 30일 30GB' WHERE product_code = 'B-103-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 매일 2GB' WHERE product_code = 'A-011-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 매일 10GB' WHERE product_code = 'A-011-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 30일 3GB' WHERE product_code = 'A-011-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 30일 8GB' WHERE product_code = 'A-011-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 30일 15GB' WHERE product_code = 'A-011-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 30일 30GB' WHERE product_code = 'A-011-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '말레이시아 365일 100GB' WHERE product_code = 'A-011-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '모잠비크 매일 1GB' WHERE product_code = 'D-012-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '모잠비크 매일 500MB' WHERE product_code = 'D-012-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니제르 매일 1GB' WHERE product_code = 'D-013-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니제르 매일 500MB' WHERE product_code = 'D-013-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '나이지리아 매일 1GB' WHERE product_code = 'D-007-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '나이지리아 매일 500MB' WHERE product_code = 'D-007-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 매일 1GB' WHERE product_code = 'F-021-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 매일 500MB' WHERE product_code = 'F-021-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 30일 3GB' WHERE product_code = 'F-021-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 30일 8GB' WHERE product_code = 'F-021-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 30일 15GB' WHERE product_code = 'F-021-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '니카라과 30일 30GB' WHERE product_code = 'F-021-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 매일 2GB' WHERE product_code = 'A-032-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 매일 500MB' WHERE product_code = 'A-032-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 30일 3GB' WHERE product_code = 'A-032-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 30일 8GB' WHERE product_code = 'A-032-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 30일 15GB' WHERE product_code = 'A-032-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 30일 30GB' WHERE product_code = 'A-032-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '네팔 365일 100GB' WHERE product_code = 'A-032-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 매일 2GB' WHERE product_code = 'C-002-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 매일 10GB' WHERE product_code = 'C-002-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 30일 3GB' WHERE product_code = 'C-002-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 30일 8GB' WHERE product_code = 'C-002-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 30일 15GB' WHERE product_code = 'C-002-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 30일 30GB' WHERE product_code = 'C-002-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '뉴질랜드 365일 100GB' WHERE product_code = 'C-002-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 매일 1GB' WHERE product_code = 'G-008-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 매일 500MB' WHERE product_code = 'G-008-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 30일 3GB' WHERE product_code = 'G-008-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 30일 8GB' WHERE product_code = 'G-008-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 30일 15GB' WHERE product_code = 'G-008-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '오만 30일 30GB' WHERE product_code = 'G-008-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 매일 1GB' WHERE product_code = 'F-007-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 매일 500MB' WHERE product_code = 'F-007-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 30일 3GB' WHERE product_code = 'F-007-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 30일 8GB' WHERE product_code = 'F-007-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 30일 15GB' WHERE product_code = 'F-007-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파나마 30일 30GB' WHERE product_code = 'F-007-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 매일 1GB' WHERE product_code = 'F-008-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 매일 500MB' WHERE product_code = 'F-008-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 30일 3GB' WHERE product_code = 'F-008-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 30일 8GB' WHERE product_code = 'F-008-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 30일 15GB' WHERE product_code = 'F-008-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '페루 30일 30GB' WHERE product_code = 'F-008-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 매일 2GB' WHERE product_code = 'A-009-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 매일 10GB' WHERE product_code = 'A-009-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 30일 3GB' WHERE product_code = 'A-009-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 30일 8GB' WHERE product_code = 'A-009-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 30일 15GB' WHERE product_code = 'A-009-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 30일 30GB' WHERE product_code = 'A-009-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '필리핀 365일 100GB' WHERE product_code = 'A-009-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 매일 2GB' WHERE product_code = 'A-031-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 매일 500MB' WHERE product_code = 'A-031-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 30일 3GB' WHERE product_code = 'A-031-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 30일 8GB' WHERE product_code = 'A-031-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 30일 15GB' WHERE product_code = 'A-031-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 30일 30GB' WHERE product_code = 'A-031-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파키스탄 365일 100GB' WHERE product_code = 'A-031-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 매일 1GB' WHERE product_code = 'F-009-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 매일 500MB' WHERE product_code = 'F-009-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 30일 3GB' WHERE product_code = 'F-009-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 30일 8GB' WHERE product_code = 'F-009-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 30일 15GB' WHERE product_code = 'F-009-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 30일 30GB' WHERE product_code = 'F-009-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '푸에르토리코 365일 100GB' WHERE product_code = 'F-009-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 매일 1GB' WHERE product_code = 'F-024-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 매일 500MB' WHERE product_code = 'F-024-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 30일 3GB' WHERE product_code = 'F-024-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 30일 8GB' WHERE product_code = 'F-024-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 30일 15GB' WHERE product_code = 'F-024-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '파라과이 30일 30GB' WHERE product_code = 'F-024-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 매일 1GB' WHERE product_code = 'G-004-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 매일 500MB' WHERE product_code = 'G-004-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 30일 3GB' WHERE product_code = 'G-004-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 30일 8GB' WHERE product_code = 'G-004-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 30일 15GB' WHERE product_code = 'G-004-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '카타르 30일 30GB' WHERE product_code = 'G-004-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '레위니옹 매일 1GB' WHERE product_code = 'D-021-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '레위니옹 매일 500MB' WHERE product_code = 'D-021-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '르완다 매일 1GB' WHERE product_code = 'D-015-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '르완다 매일 500MB' WHERE product_code = 'D-015-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 매일 1GB' WHERE product_code = 'G-003-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 매일 500MB' WHERE product_code = 'G-003-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 30일 3GB' WHERE product_code = 'G-003-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 30일 8GB' WHERE product_code = 'G-003-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 30일 15GB' WHERE product_code = 'G-003-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 30일 30GB' WHERE product_code = 'G-003-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사우디아라비아 30일 100GB' WHERE product_code = 'G-003-T-AU-Glo-30D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세이셸 매일 1GB' WHERE product_code = 'D-014-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세이셸 매일 500MB' WHERE product_code = 'D-014-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 매일 2GB' WHERE product_code = 'A-019-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 매일 10GB' WHERE product_code = 'A-019-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 30일 3GB' WHERE product_code = 'A-019-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 30일 8GB' WHERE product_code = 'A-019-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 30일 15GB' WHERE product_code = 'A-019-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 30일 30GB' WHERE product_code = 'A-019-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '싱가포르 365일 100GB' WHERE product_code = 'A-019-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 매일 2GB' WHERE product_code = 'C-005-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 매일 2GB' WHERE product_code = 'C-103-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 매일 500MB' WHERE product_code = 'C-005-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 매일 500MB' WHERE product_code = 'C-103-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 30일 3GB' WHERE product_code = 'C-005-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 30일 8GB' WHERE product_code = 'C-005-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 30일 3GB' WHERE product_code = 'C-103-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 30일 8GB' WHERE product_code = 'C-103-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 30일 15GB' WHERE product_code = 'C-005-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '사이판 30일 30GB' WHERE product_code = 'C-005-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 30일 15GB' WHERE product_code = 'C-103-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '괌·사이판 30일 30GB' WHERE product_code = 'C-103-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 매일 1GB' WHERE product_code = 'F-037-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 매일 500MB' WHERE product_code = 'F-037-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 30일 3GB' WHERE product_code = 'F-037-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 30일 8GB' WHERE product_code = 'F-037-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 30일 15GB' WHERE product_code = 'F-037-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '수리남 30일 30GB' WHERE product_code = 'F-037-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 매일 1GB' WHERE product_code = 'F-015-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 매일 500MB' WHERE product_code = 'F-015-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 30일 3GB' WHERE product_code = 'F-015-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 30일 8GB' WHERE product_code = 'F-015-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 30일 15GB' WHERE product_code = 'F-015-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '엘살바도르 30일 30GB' WHERE product_code = 'F-015-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 매일 1GB' WHERE product_code = 'F-035-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 매일 500MB' WHERE product_code = 'F-035-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 30일 3GB' WHERE product_code = 'F-035-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 30일 8GB' WHERE product_code = 'F-035-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 30일 15GB' WHERE product_code = 'F-035-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '터크스 케이커스 제도 30일 30GB' WHERE product_code = 'F-035-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 매일 2GB' WHERE product_code = 'A-007-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 매일 10GB' WHERE product_code = 'A-007-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 30일 3GB' WHERE product_code = 'A-007-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 30일 8GB' WHERE product_code = 'A-007-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 30일 15GB' WHERE product_code = 'A-007-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 30일 30GB' WHERE product_code = 'A-007-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '태국 365일 100GB' WHERE product_code = 'A-007-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 매일 2GB' WHERE product_code = 'A-164-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 매일 10GB' WHERE product_code = 'A-164-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 30일 8GB' WHERE product_code = 'A-164-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 30일 15GB' WHERE product_code = 'A-164-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 30일 30GB' WHERE product_code = 'A-164-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '동남아 6개국 365일 100GB' WHERE product_code = 'A-164-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀니지 매일 1GB' WHERE product_code = 'D-023-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀니지 매일 500MB' WHERE product_code = 'D-023-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀르키예 30일 3GB' WHERE product_code = 'E-015-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀르키예 30일 8GB' WHERE product_code = 'E-015-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀르키예 30일 15GB' WHERE product_code = 'E-015-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀르키예 30일 30GB' WHERE product_code = 'E-015-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '튀르키예 365일 100GB' WHERE product_code = 'E-015-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 매일 2GB' WHERE product_code = 'A-004-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 매일 10GB' WHERE product_code = 'A-004-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 30일 3GB' WHERE product_code = 'A-004-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 30일 8GB' WHERE product_code = 'A-004-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 30일 15GB' WHERE product_code = 'A-004-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 30일 30GB' WHERE product_code = 'A-004-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '대만 365일 100GB' WHERE product_code = 'A-004-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '탄자니아 매일 1GB' WHERE product_code = 'D-016-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '탄자니아 매일 500MB' WHERE product_code = 'D-016-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우크라이나 30일 3GB' WHERE product_code = 'E-055-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우크라이나 30일 8GB' WHERE product_code = 'E-055-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우크라이나 30일 15GB' WHERE product_code = 'E-055-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우크라이나 30일 30GB' WHERE product_code = 'E-055-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우간다 매일 1GB' WHERE product_code = 'D-017-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우간다 매일 500MB' WHERE product_code = 'D-017-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 매일 2GB' WHERE product_code = 'B-001-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 매일 10GB' WHERE product_code = 'B-001-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 무제한' WHERE product_code = 'B-001-T-AU-Glo-30D/D-U' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 3GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 5GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-5GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 8GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 10GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-10GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 15GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 30GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 50GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 30일 100GB' WHERE product_code = 'B-001-T-AU-Glo-30D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 365일 25GB' WHERE product_code = 'B-001-T-AU-Glo-365D/D-25GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 365일 50GB' WHERE product_code = 'B-001-T-AU-Glo-365D/D-50GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 365일 100GB' WHERE product_code = 'B-001-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 365일 200GB' WHERE product_code = 'B-001-T-AU-Glo-365D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 365일 300GB' WHERE product_code = 'B-001-T-AU-Glo-365D/D-300GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 730일 200GB' WHERE product_code = 'B-001-T-AU-Glo-730D/D-200GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 730일 300GB' WHERE product_code = 'B-001-T-AU-Glo-730D/D-300GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 730일 500GB' WHERE product_code = 'B-001-T-AU-Glo-730D/D-500GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 매일 1GB' WHERE product_code = 'F-110-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국 매일 500MB' WHERE product_code = 'F-110-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아메리카 39개국 30일 8GB' WHERE product_code = 'F-110-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아메리카 39개국 30일 15GB' WHERE product_code = 'F-110-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '아메리카 39개국 30일 30GB' WHERE product_code = 'F-110-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 매일 1GB' WHERE product_code = 'F-019-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 매일 500MB' WHERE product_code = 'F-019-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 30일 3GB' WHERE product_code = 'F-019-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 30일 8GB' WHERE product_code = 'F-019-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 30일 15GB' WHERE product_code = 'F-019-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우루과이 30일 30GB' WHERE product_code = 'F-019-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 매일 1GB' WHERE product_code = 'G-011-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 매일 500MB' WHERE product_code = 'G-011-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 30일 3GB' WHERE product_code = 'G-011-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 30일 8GB' WHERE product_code = 'G-011-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 30일 15GB' WHERE product_code = 'G-011-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 30일 30GB' WHERE product_code = 'G-011-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '우즈베키스탄 365일 100GB' WHERE product_code = 'G-011-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 매일 1GB' WHERE product_code = 'F-034-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 매일 500MB' WHERE product_code = 'F-034-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 30일 3GB' WHERE product_code = 'F-034-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 30일 8GB' WHERE product_code = 'F-034-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 30일 15GB' WHERE product_code = 'F-034-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '세인트빈센트 그레나딘 30일 30GB' WHERE product_code = 'F-034-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 매일 1GB' WHERE product_code = 'F-006-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 매일 500MB' WHERE product_code = 'F-006-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 30일 3GB' WHERE product_code = 'F-006-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 30일 8GB' WHERE product_code = 'F-006-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 30일 15GB' WHERE product_code = 'F-006-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베네수엘라 30일 30GB' WHERE product_code = 'F-006-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 매일 1GB' WHERE product_code = 'F-012-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 매일 500MB' WHERE product_code = 'F-012-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 30일 3GB' WHERE product_code = 'F-012-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 30일 8GB' WHERE product_code = 'F-012-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 30일 15GB' WHERE product_code = 'F-012-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '영국령 버진아일랜드 30일 30GB' WHERE product_code = 'F-012-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 매일 1GB' WHERE product_code = 'F-026-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 매일 500MB' WHERE product_code = 'F-026-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 30일 3GB' WHERE product_code = 'F-026-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 30일 8GB' WHERE product_code = 'F-026-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 30일 15GB' WHERE product_code = 'F-026-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '미국령 버진아일랜드 30일 30GB' WHERE product_code = 'F-026-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 매일 2GB' WHERE product_code = 'A-008-AU-D-D-Glo-2GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 매일 10GB' WHERE product_code = 'A-008-AU-D-D-Glo-10GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 30일 3GB' WHERE product_code = 'A-008-T-AU-Glo-30D/D-3GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 30일 8GB' WHERE product_code = 'A-008-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 30일 15GB' WHERE product_code = 'A-008-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 30일 30GB' WHERE product_code = 'A-008-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '베트남 365일 100GB' WHERE product_code = 'A-008-T-AU-Glo-365D/D-100GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '남아프리카공화국 매일 1GB' WHERE product_code = 'D-001-AU-D-D-Glo-1GBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '남아프리카공화국 매일 500MB' WHERE product_code = 'D-001-AU-D-D-Glo-500MBH' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '남아프리카공화국 30일 8GB' WHERE product_code = 'D-001-T-AU-Glo-30D/D-8GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '남아프리카공화국 30일 15GB' WHERE product_code = 'D-001-T-AU-Glo-30D/D-15GB' AND (display_name IS NULL OR display_name = '');
UPDATE TB_WIFI_PRODUCT SET display_name = '남아프리카공화국 30일 30GB' WHERE product_code = 'D-001-T-AU-Glo-30D/D-30GB' AND (display_name IS NULL OR display_name = '');

-- 5) 중국 상품(전부 TUGE (V) = 해외 앱 되는 버전): 표시명에 고객이 알아보는 문구
UPDATE TB_WIFI_PRODUCT
SET display_name = CONCAT(display_name, ' (카톡·구글 가능)')
WHERE (product_code LIKE 'A-003-VPN-%' OR product_code LIKE 'A-136-%')
  AND display_name IS NOT NULL AND display_name NOT LIKE '%카톡·구글 가능%';
