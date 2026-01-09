package com.naver.naverspabackend.response;

import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.util.PagingUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ApiResult<T> {

    private static final String SUCCESS = "succ";

    private static final String FAIL = "fail";

    private final T data;

    private final String result;

    private final String message;

    private final ResponseCode responseCode;

    private final PagingUtil pagingUtil;

    ApiResult(T data, String result, String message, ResponseCode responseCode, PagingUtil pagingUtil) {
        this.data = data;
        this.result = result;
        this.message = message;
        this.responseCode = responseCode;
        this.pagingUtil = pagingUtil;
    }

    public static <T> ApiResult<T> succeed(T data, PagingUtil pagingUtil) {
        return new ApiResult<>(data, SUCCESS, null, ResponseCode.SUCC, pagingUtil);
    }

    public static <T> ApiResult<T> succeed(T data, String message, PagingUtil pagingUtil) {
        return new ApiResult<>(data, SUCCESS, message, ResponseCode.SUCC, pagingUtil);
    }

    public static ApiResult<?> failed(Throwable throwable, ResponseCode code) {
        return failed(throwable.getMessage(), code);
    }

    public static ApiResult<?> failed(String message, ResponseCode code) {
        return new ApiResult<>(null, FAIL, message, code, null);
    }

    public String getResult() {
        return result;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public PagingUtil getPage() {return pagingUtil;}

    public ResponseCode getResponseCode(){return responseCode;}

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("data", data).append("message", message).toString();
    }
}