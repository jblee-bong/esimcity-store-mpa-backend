package com.naver.naverspabackend.service.esimapiingsteplogs;

import com.naver.naverspabackend.dto.EsimApiIngStepLogsDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public interface EsimApiIngStepLogsService {
    void insert(String esimLogs , Long orderId);

    void insertRest(HttpHeaders headers, Object jsonObject, Long orderId);
}
