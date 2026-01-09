package com.naver.naverspabackend.service.sms;

import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.Map;

public interface SmsService {

    int insertSms(Map searchMap, StoreDto storeDto) throws Exception;
    int insertMms(Map searchMap, StoreDto storeDto) throws Exception;

    ApiResult<?> fetchSmsSendList(Map<String, Object> paramMap, PagingUtil pagingUtil);

    ApiResult<?> fetchMailSendList(Map<String, Object> paramMap, PagingUtil pagingUtil);
}
