package com.naver.naverspabackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 네이버 검색광고 자동입찰 전역 설정.
 */
@Data
@ConfigurationProperties(prefix = "naver-search-ad")
public class NaverSearchAdProperties {

    private boolean enabled = false;

    private String baseUrl = "https://api.searchad.naver.com";

    private String accessLicense;

    private String secretKey;

    private String customerId;

    private int targetPosition = 3;

    private String device = "MOBILE";
    private String device2 = "PC";

    private int minBid = 70;

    private int maxBid = 50000;

    /**
     * 쇼핑검색: NPLA 추정가와의 차이 허용(원) — 이하면 유지
     * (파워링크 bidTolerance 와 동일 설정 재사용)
     */
    private int bidTolerance = 10;

    /**
     * 파워링크 delta: |deltaRnk - 목표| 이하면 입찰 유지
     */
    private double rankTolerance = 0.3;

    /** 입찰 조정 단위(원) — 쇼핑 NPLA step / 파워링크 delta step */
    private int bidStep = 50;

    /**
     * 파워링크: Δimp 가 이 값 미만이면 delta 순위 신뢰 불가 → 입찰 유지
     */
    private int minDeltaImp = 20;
}
