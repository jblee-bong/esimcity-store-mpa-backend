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
    private String deviceType;
    private BigDecimal settlementUsd;
    private String popupJson;
    private String activeType;
    private String ruleDesc;
    private String tugeModifiedDt;
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

    /** 기간 배수 주문 가능 여부 (문서 3.4 orderPeriod / 오류 1011: 1일 DAILY, 1개월 MONTH 만 가능) */
    public boolean isPeriodSelectable() {
        return days != null && days == 1 && ("DAILY".equals(productType) || "MONTH".equals(productType));
    }
}
