package com.naver.naverspabackend.enums;

import lombok.Getter;

@Getter
public enum KakaoTemplate {

    sample("A001");

    String kakaoTemplateKey;

    KakaoTemplate(String kakaoTemplateKey) {
        this.kakaoTemplateKey = kakaoTemplateKey;
    }
}
