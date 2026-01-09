package com.naver.naverspabackend.controller;

import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.ExcelFile;
import com.naver.naverspabackend.dto.EsimApiDto;
import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.sms.DashboardService;
import com.naver.naverspabackend.service.sms.KakaoService;
import com.naver.naverspabackend.service.sms.MatchInfoService;
import com.naver.naverspabackend.service.sms.SmsService;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.JsonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@RestController
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;


    @RequestMapping("/dashboard/fetchStoreList")
    public ApiResult<?> fetchStoreList( @TokenUser UserDto userDto1){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());
        return dashboardService.fetchDashboardStoreList(paramMap);
    }

    @RequestMapping("/dashboard/fetchDataList")
    public ApiResult<?> fetchDashboardList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());
        return dashboardService.fetchDashboardList(paramMap);
    }

}
