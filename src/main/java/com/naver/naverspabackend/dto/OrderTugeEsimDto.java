package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderTugeEsimDto extends BaseDto{

    //excel
    private Long id;
    private Long orderId;
    private String orderNo;
    private String iccid;
    private String qrCode;
    private String channelOrderNo;
    private String imsi;
    private String msisdn;
    private String activatedStartTime;
    private String activatedEndTime;
    private String latestActivationTime;
    private String renewExpirationTime;
    private String orderType;

}
