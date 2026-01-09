package com.naver.naverspabackend.service.sms;

import com.naver.naverspabackend.dto.OrderDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.Map;

public interface KakaoService {

    /**
     * @templateKey
     * @author 박상길
     */

    int requestSendKakaoMsg(Map<String, Object> kakaoParameters, String templateKey, StoreDto storeDto, OrderDto orderDto,String esimFlag,String kakaoResendFlag, boolean retrans, String... noList) throws Exception;

    ApiResult<?> fetchKakaoSendList(Map<String, Object> paramMap, PagingUtil pagingUtil);

    ApiResult<?> fetchTemplateList(StoreDto data);
}
