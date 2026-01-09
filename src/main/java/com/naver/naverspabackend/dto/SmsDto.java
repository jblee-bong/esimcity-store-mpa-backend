package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsDto extends BaseDto{

    private Long seq;
    private String headerResultCode;
    private String headerResultMessage;
    private String headerIsSuccessful;
    private String messageRequestId;
    private String messageStatusCode;
    private String messageRecipientNo;
    private String messageRecipientSeq;
    private String messageRecipientMessage;
    private String messageRecipientCode;
    private String sender;
    private String senderNo;
    private String regDate;
    private String templateContents;
    private String type;
    private String templateTitle;


    private Long storeId;
    private Long originProductNo;
    private Long optionId;
}
