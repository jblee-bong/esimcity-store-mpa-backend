package com.naver.naverspabackend.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiProductDto {
    private String productCode;
    private String tugeName;
    private String productType;     // DAILY / MONTH / MULTIPLEMONTHS
    private Integer days;
    private Integer validityPeriod;
    private String dataLimited;
    private String dataTotal;
    private String dataUnit;
    private Integer dailyHighSpeedMb;
    private String mcc;
    private String afterUse;         // 다 쓰면: SLOW / STOP / null(자동)
    private String countryOverride;  // 관리자 나라 직접 지정 (ISO 콤마), 비우면 MCC 자동
    private String deviceType;
    private BigDecimal settlementUsd;
    private String popupJson;
    private String activeType;
    private String ruleDesc;
    private String tugeModifiedDt;
    private String highSpeed;        // TUGE highSpeed "2048(M)"
    private String limitSpeed;       // TUGE limitSpeed "2000(K)"
    private String showGradeNumber;
    private String countryCodeList;  // TUGE countryCodeList (ISO)
    private String periodType;       // BYDAY / BYMONTH
    private String isContract;
    private Integer minOrderCycle;
    private BigDecimal firstOrderPrice; // TUGE firstOrderPrice (첫 주문가, 보통 null)
    private String rawJson;          // TUGE 상품 원본
    private String tugeActive;
    private String costChangedYn;
    private String saleYn;
    private Long categoryId;        // 나라 (가격정책은 이 나라 → 상위 지역 → 기본값)
    private int minPeriod = 1;
    private int maxPeriod = 30;
    private String displayName;
    private String thumbnail;
    private String detailHtml;
    private int sort;
    private Date registDt;
    private Date modifyDt;

    /* 계산값 (DB 컬럼 아님) - WifiPricing */
    private Integer saleKrw;        // 기간선택 상품: 1일(1개월) 가격 / 고정 상품: 상품 가격
    private int[] prices;           // 기간별 총액: prices[n-1] = n일(월) 가격 (상세 화면용)
    private java.util.List<java.util.Map<String, String>> countries; // 쓸 수 있는 나라 [{iso, name}] (MCC 로 계산)
    private java.util.List<String> unknownMcc; // MCC 표에 없는 번호 (관리자 경고)
    private java.util.List<String> matched;  // 여러 나라 검색: 고른 나라 중 되는 나라(ISO)
    private java.util.List<String> missing;  // 여러 나라 검색: 고른 나라 중 안 되는 나라(ISO)

    /** 다 쓰면 저속으로 계속 쓸 수 있는지: 관리자 지정이 우선, 없으면 TUGE dataLimited(N=용량 제한 없음 → 저속 계속) */
    public boolean isSlowAfter() {
        if ("SLOW".equals(afterUse)) return true;
        if ("STOP".equals(afterUse)) return false;
        return !"Y".equals(dataLimited);
    }

    /** 날짜 지정 상품(TUGE ACTIVEDBYORDER): 고객이 고른 시작일 00:00(KST)부터. 그 외는 장비가 처음 연결된 날부터 */
    public boolean isDateStart() {
        return "ACTIVEDBYORDER".equals(activeType);
    }

    /**
     * 날짜 지정 상품의 시작일로 고를 수 있는 범위(오늘부터 N일).
     * validityPeriod 는 쓰지 않음: 샌드박스 데일리가 validityPeriod=1 인데 10일 뒤 시작 주문도 정상 접수됨(2026-11-18)
     * shortcut: 60일 고정, TUGE 가 미래 시작일 한도를 알려 주면 그 값으로
     */
    public int startWithinDays() {
        return 60;
    }

    /** 기간(일수) 선택 가능 여부: 1일 데일리만. 월간은 1개월 고정으로 팔고 필요하면 다시 충전 (정책) */
    public boolean isPeriodSelectable() {
        return days != null && days == 1 && "DAILY".equals(productType);
    }
}
