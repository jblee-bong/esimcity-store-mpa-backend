package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.annotation.ExcelColumnName;
import com.naver.naverspabackend.util.DateConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class OrderWorldmoveEsimDto {

    //excel
    private Long id;
    private String esimQrText;
    private String esimType;
    private Long orderId;
    private String esimIccid;


    private String apnExplain;

}
