package com.naver.naverspabackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoContentsDto extends BaseDto{

    private Long id;
    private String kakaoParameter;
    private String kakaoTemplateKey;

    private String esimFlag;
    private Long storeId;
    private Long orderId;

}
