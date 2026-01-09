package com.naver.naverspabackend.dto;

import com.naver.naverspabackend.annotation.ExcelColumnName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsimApiDto {

    @ExcelColumnName(headerName = "상품ID")
    private String productId;
    @ExcelColumnName(headerName = "타이틀")
    private String description;
    @ExcelColumnName(headerName = "days")
    private String days;



    @ExcelColumnName(headerName = "가격")
    private String apiPurchasePrice; // 가격
    @ExcelColumnName(headerName = "단위")
    private String apiPurchaseCurrency; // 단위

}
