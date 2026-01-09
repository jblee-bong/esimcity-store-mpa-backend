package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailDto extends BaseDto{

    private Long seq;

    private String fromEmail;
    private String fromUsername;
    private String fromEmailPw;
    private String smtpUrl;
    private Integer port;


    private String toEmail;
    private String[] address;
    private String subject;
    private String contents;
    private String template;


    private Long storeId;
    private Long originProductNo;
    private Long optionId;

}
