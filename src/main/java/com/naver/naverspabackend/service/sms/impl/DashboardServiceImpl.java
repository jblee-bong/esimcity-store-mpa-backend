package com.naver.naverspabackend.service.sms.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.dto.KakaoDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.DashboardMapper;
import com.naver.naverspabackend.mybatis.mapper.KakaoMsgMapper;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.sms.DashboardService;
import com.naver.naverspabackend.util.ApiUtil;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private StoreMapper storeMapper;


    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public ApiResult<?> fetchDashboardList(Map<String, Object> paramMap) {

        return ApiResult.succeed(dashboardMapper.selectDashboardListWithStoreId(paramMap), null);
    }

    @Override
    public ApiResult<?> fetchDashboardStoreList(Map<String, Object> paramMap) {
        return ApiResult.succeed(storeMapper.selectStoreListAll(paramMap), null);
    }
}
