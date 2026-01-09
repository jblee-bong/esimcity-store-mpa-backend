package com.naver.naverspabackend.enums;

import lombok.Getter;

@Getter
public enum EsimApiIngSteLogsType {
    START("start", "API 호출 시작"),
    PURCHASE("purchase", "구매 시작"),
    PURCHASE_END0("purchaseEnd0", " 구매 완료 0 "),
    PURCHASE_END("purchaseEnd", " 구매 완료 "),
    PURCHASE_END_FAIL("purchaseEndFail", " 구매 실패 "),
    STATUS("status", "상태 체크 시작"),
    STATUS_END0("statusEnd","상태 체크 완료 0 "),
    STATUS_END("statusEnd","상태 체크 완료 "),
    ACCESS_TOKEN("accessToken","토큰 시작"),
    ACCESS_TOKEN_END("accessTokenEnd","토큰 완료"),
    TOPUP("topup", "충전 시작"),
    TOPUP_END("topupEnd", " 충전 종료 "),



    PARAM("param", "API 호출 데이터 "),
    ERROR("error", "에러 발생 "),


    OPEN_END("openEnd", "개통완료 "),

    ;

    String name;
    String explain;

    EsimApiIngSteLogsType(String name, String explain) {
        this.name = name;
        this.explain = explain;
    }

}
