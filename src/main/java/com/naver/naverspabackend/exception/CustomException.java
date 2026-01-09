package com.naver.naverspabackend.exception;

import com.naver.naverspabackend.response.model.ResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    ResponseCode responseCode;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}