package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailContentsDto {

    private Long id;
    private String emailContents;
    private String emailSubject;

    private Long storeId;
    private Long originProductNo;
    private Long optionId;
    private Long orderId;

    private String esimYn;

}
