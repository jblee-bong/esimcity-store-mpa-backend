package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoDto extends BaseDto{

    private Long seq;
    private String headerResultCode;
    private String headerResultMessage;
    private String headerIsSuccessful;
    private String messageRequestId;
    private String messageRecipientSeq;
    private String messageRecipientNo;
    private String messageRecipientCode;
    private String messageRecipientMessage;
    private String templateKey;
    private String statsId;

    private Long storeId;
    private Long originProductNo;
    private Long optionId;

    private String regDate;
}
