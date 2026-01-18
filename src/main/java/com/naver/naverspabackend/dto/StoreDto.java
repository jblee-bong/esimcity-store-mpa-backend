package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreDto extends BaseDto{

    private Long id;

    private String storeName;

    private String clientId;

    private String clientSecret;

    private String vendorId;

    private String platform;

    private String sendEmail;


    private String sendEmailPw;

    private String sendEmailUsername;

    private String sendEmailSmtpUrl;
    private Integer sendEmailSmtpPort;

    private String sendPhone;


    private String nhnSmsAppKey;
    private String nhnSmsSecretKey;
    private String nhnKakaoAppKey;
    private String nhnKakaoSecretKey;
    private String nhnKakaoSenderKey;

    private String smsTemplate;
    private String mmsTitleTemplate;
    private String mmsBodyTemplate;
    private String kakaoBodyTemplate;
    private String emailTitleTemplate;
    private String emailBodyTemplate;



    private String comfirmFlag;
    private String comfirmSmsTemplate;
    private String comfirmMmsTitleTemplate;
    private String comfirmMmsBodyTemplate;
    private String comfirmKakaoBodyTemplate;
    private String comfirmEmailTitleTemplate;
    private String comfirmEmailBodyTemplate;

    private String esimMailTemplate;
    private String esimMailTitleTemplate;
    private String esimKakaoTemplate;
    private String esimKakaoResendFlagTemplate;
    private String esimTitleTemplate;
    private String esimBodyTemplate;
    private String esimSmsTemplate;


    private String esimFlag;
    private String sendMethod;
    private String orderingUseYn;
    private String transUseYn;
    private String useYn;
    private String esimCode;
    private String esimType;
    private String esimProductId;
    private String esimProductDays;
    private String esimDescription;


    private int relationProductCnt;


    private String esimCopyWrite;
    private String esimQuestLink;
    private String esimLogoLink;
    private String esimLogoType;



    private String esimApiTsimAccount;
    private String esimApiTsimSecret;
    private String esimApiTugeAccount;
    private String esimApiTugeSecret;
    private String esimApiTugeSign;
    private String esimApiTugeVector;
    private String esimApiWorldMoveMerchantId;
    private String esimApiWorldMoveDeptId;
    private String esimApiWorldMoveToken;
    private String esimApiNizId;
    private String esimApiNizPass;
    private String esimApiNizPartnerCode;
    private String esimApiAiraloClientId;
    private String esimApiAiraloClientSecret;


    private String esimApiEsimaccessClientId;
    private String esimApiEsimaccessClientSecret;

    public StoreDto() {
        super();
    }
    public StoreDto(long id) {
        super();
        this.id = id;
    }
}
