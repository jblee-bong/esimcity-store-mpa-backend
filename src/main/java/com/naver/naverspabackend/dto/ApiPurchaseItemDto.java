package com.naver.naverspabackend.dto;

import com.naver.naverspabackend.annotation.ExcelColumnName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiPurchaseItemDto extends BaseDto {

    private String apiPurchaseItemProcutId;//상품ID

    private String apiPurchaseItemType;//상품타입

    private String apiPurchaseItemDescription;

    private String apiPurchaseItemSelectType;

    private String apiPurchaseItemDays; // days


    private String apiPurchasePrice; // 가격
    private String apiPurchaseKrwPrice; // 한국가격
    private String apiPurchaseCurrency; // 단위

    private String apiPurchaseProductType; // tuge를 위한 값. 데일리지인 아닌지


    private String apiPurchaseDataTotal; // tuge를 위한 값. 데이터총량
    private String apiPurchaseDataUnit; // tuge를 위한 값. 데이터단위

    private String apiPurchaseNormalSpeed; // 기본제공속도
    private String apiPurchaseSlowSpeed; // tuge를 위한 값. 데이터단위
    private String apiPurchaseUnusedValidTime; // 구매후 몇일 내 사용

    private String apiPurchaseCoverDomainCode;//국가코드
    private boolean apiPurchaseItemIsDaily;//데일리상품인지유무
    private String apiPurchaseApn; //apn
    private String apiPurchaseExportDomainCode; //나가는 도메인 코드

    private String searchKeyword;


    private boolean apiPurchaseIsCharge;//충전가능상품유무


    private String apiPurchaseItemCardType; // 카드타입
    private Integer apiPurchaseItemPeriodType; // 일자계산타입 0: 활성화후 24시간, 1: 카드타입별 타임존 00시

    public ApiPurchaseItemDto(String apiPurchaseItemType, String searchKey) {
        super();
        this.apiPurchaseItemType = apiPurchaseItemType;
        this.searchKeyword = searchKey;
    }
    public ApiPurchaseItemDto() {
        super();
    }
}
