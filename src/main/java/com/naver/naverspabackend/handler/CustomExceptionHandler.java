package com.naver.naverspabackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice("")
public class CustomExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResult<?> commonException(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(ResponseCode.ABNORMAL.getHttpStatus().value());
        objectMapper.writeValue(response.getWriter(),ApiResult.failed(e, ResponseCode.ABNORMAL));
        return ApiResult.failed(e, ResponseCode.ABNORMAL);
    }

    @ExceptionHandler(CustomException.class)
    public ApiResult<?> customException(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(),ApiResult.failed(e, ResponseCode.ABNORMAL));
        return ApiResult.failed(e, ResponseCode.ABNORMAL);
    }

}
