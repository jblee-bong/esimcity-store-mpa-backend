package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiCategoryDto {
    private Long id;
    private Long parentId;      // NULL = 지역(1단계), 값 = 나라(2단계)
    private String name;
    private String image;
    private int sort;
    private String useYn;
    /* 가격정책 (비어 있으면 상위 지역 → 코드관리 WIFI 기본값) */
    private Integer usdRate;        // 충전 1달러당 원가(원)
    private Double marginRate;      // 기본 마진율(%)
    private String periodRules;     // 일수별 마진율 JSON [{"from":7,"rate":25},{"from":30,"rate":15}]
    private int productCount;   // 관리자 목록용
}
