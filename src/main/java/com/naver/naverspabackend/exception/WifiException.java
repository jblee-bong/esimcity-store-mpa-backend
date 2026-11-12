package com.naver.naverspabackend.exception;

import com.naver.naverspabackend.response.model.ResponseCode;
import lombok.Getter;

/** 포켓와이파이 API 사용자 노출용 오류 (메시지를 그대로 화면에 보여줌) */
@Getter
public class WifiException extends RuntimeException {

    private final ResponseCode responseCode;

    public WifiException(String message) {
        this(message, ResponseCode.ABNORMAL);
    }

    public WifiException(String message, ResponseCode responseCode) {
        super(message);
        this.responseCode = responseCode;
    }
}
