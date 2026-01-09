package com.naver.naverspabackend.service.sms;

import com.naver.naverspabackend.response.ApiResult;

import java.util.Map;

public interface DashboardService {

    /**
     * @templateKey
     * @author 이재봉
     */

    ApiResult<?> fetchDashboardList(Map<String, Object> paramMap);

    ApiResult<?> fetchDashboardStoreList(Map<String, Object> paramMap);
}
