package com.naver.naverspabackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naver.naverspabackend.annotation.ExcelColumnName;
import com.naver.naverspabackend.util.DateConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderRetransMailInfoDto extends BaseDto {

    private Long id;
    /**
     * order 관련
     */
    // order id 주문
    private String orderId;

    private String mail; // 메일주소

    private Integer sendStatus; //0 발송전, 1 발송완료

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConstants.datePattern, timezone = DateConstants.timeZone)
    private Date sendDateTime;
}
