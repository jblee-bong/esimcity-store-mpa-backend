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
public class BulkDto extends BaseDto {


    @ExcelColumnName(headerName = "rental_no")
    private String rentalNo;//rentalNo

    @ExcelColumnName(headerName = "smdp")
    private String bulkSmdp;//smdp

    @ExcelColumnName(headerName = "active_code")
    private String bulkActiveCode;//active_code

    @ExcelColumnName(headerName = "iccid")
    private String bulkIccid; // iccid

    @ExcelColumnName(headerName = "title")
    private String bulkTitle; //title

    @ExcelColumnName(headerName = "bulkOpenDt")
    private String bulkOpenDt;

    @ExcelColumnName(headerName = "bulkExpiredDay")
    private String bulkExpiredDay;

    @ExcelColumnName(headerName = "bulkExpiredDt")
    private String bulkExpiredDt;


    @ExcelColumnName(headerName = "useYn")
    private String useYn; //사용유무

    // id auto_increment
    private Long id;
    //excel

    private Long orderId;//TB_ORDER_ID

}
