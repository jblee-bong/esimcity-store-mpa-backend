package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsimPriceDto {

    private String type;
    private Double exchangeRate;
    private Double exchangeWeight;
    private Double weight1;
    private Double weight2;
    private Double weight3;
    private Double weight4;
    private Double weight5;

}
