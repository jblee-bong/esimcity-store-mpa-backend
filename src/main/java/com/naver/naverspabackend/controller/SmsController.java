package com.naver.naverspabackend.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.ExcelFile;
import com.naver.naverspabackend.dto.*;
import com.naver.naverspabackend.response.ApiResult;
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
public class SmsController {

    @Autowired
    private KakaoService kakaoService;


    @Autowired
    private StoreService storeService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private MatchInfoService matchInfoService;

    @RequestMapping("/sms/kakao/fetchList")
    public ApiResult<?> fetchKakaoSendList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());

        return kakaoService.fetchKakaoSendList(paramMap, pagingUtil);
    }

    @RequestMapping("/sms/sms/fetchList")
    public ApiResult<?> fetchSmsSendList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());

        return smsService.fetchSmsSendList(paramMap, pagingUtil);
    }


    @RequestMapping("/sms/mail/fetchList")
    public ApiResult<?> fetchMailSendList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());

        return smsService.fetchMailSendList(paramMap, pagingUtil);
    }


    @RequestMapping("/sms/kakao/templateList")
    public ApiResult<?> fetchTemplateList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());

        if(paramMap.get("id").equals("all")){

            return ApiResult.succeed(null, null);
        }
        ApiResult<StoreDto> storeDto = storeService.fetchStore(paramMap);
        return kakaoService.fetchTemplateList(storeDto.getData());
    }

    @RequestMapping("/sms/create")
    public ApiResult<?> createMatchInfo(@RequestBody Map<String, Object> paramMap) throws Exception {
        return matchInfoService.inserMatchInfo(paramMap);
    }

    @RequestMapping("/sms/match/fetchList")
    public ApiResult<?> fetchMatchInfoList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil) throws Exception {
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());
        return matchInfoService.fetchMatchInfoList(paramMap, pagingUtil);
    }


    @PostMapping(value = "/sms/match/fetchListExceldownload")
    public void fetchListExceldownload(HttpServletResponse response, @RequestBody Map<String, Object> map, @TokenUser UserDto userDto1) throws Exception {
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());
        List<MatchInfoDto> matchInfoDtoList = matchInfoService.fetchMatchInfoListForExcel(map);

        SXSSFWorkbook wb = null;
        OutputStream stream = null;

        try {
            ExcelFile<MatchInfoDto> excelFile = new ExcelFile<>();
            wb = excelFile.renderExcel(matchInfoDtoList, MatchInfoDto.class, null);

            stream = response.getOutputStream();
            wb.write(stream);
            wb.dispose();
        } finally {
            wb.close();
            stream.close();
        }
    }


    @PostMapping(value = "/sms/match/fetchListExcelUpload")
    public ApiResult<?> fetchListExcelUpload(@RequestParam(name="multipartFile") MultipartFile file, @TokenUser UserDto userDto1) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("email",userDto1.getEmail());
        map.put("userAuthority",userDto1.getUserAuthority().name());

        ExcelFile<MatchInfoDto> excelFile = new ExcelFile<>();
        List<Map<String,Object>> items = excelFile.uploadExcel(file.getInputStream(), MatchInfoDto.class);


        TypeToken<ArrayList<MatchInfoDto>> token = new TypeToken<ArrayList<MatchInfoDto>>() {};
        List<MatchInfoDto> matchInfoDtoList = JsonUtil.fromJson(JsonUtil.toJson(items), token.getType());
        Gson gson = new Gson();
        for(MatchInfoDto item:matchInfoDtoList){

            String content =item.getMailContents().replaceAll("\n","").replaceAll("\t","").replaceAll("\"","'");

            content = content.replaceAll("\\['","\\[\"");
            content = content.replaceAll("']","\"]");
            content = content.replaceAll("\\\\'","'");

            item.setMailContents(content);
        }

        return matchInfoService.fetchListExcelUpload(matchInfoDtoList);
    }

    @RequestMapping("/sms/match/fetch")
    public ApiResult<?> fetchMatchInfo(@RequestBody Map<String, Object> paramMap) throws Exception {
        return matchInfoService.fetchMatchInfo(paramMap);
    }

    @RequestMapping("/sms/match/update")
    public ApiResult<?> updateMatchInfo(@RequestBody Map<String, Object> paramMap) throws Exception {
        return matchInfoService.updateMatchInfo(paramMap);
    }

    @RequestMapping("/sms/match/delete")
    public ApiResult<?> deleteMatchInfo(@RequestBody Map<String, Object> paramMap) throws Exception {
        return matchInfoService.deleteMatchInfo(paramMap);
    }

    @RequestMapping("/sms/match/fetchESimProductIdList")
    public ApiResult<?> fetchMailSendList(@RequestBody Map<String, Object> paramMap) throws Exception {
        return matchInfoService.fetchEsimProductIdList(paramMap);
    }



    @RequestMapping("/sms/match/fetchEsimProductIdExcelDownload")
    public void fetchEsimProductIdExcelDownload(HttpServletResponse response, @RequestBody List<Map<String, Object>> maps) throws IOException {
        List<EsimApiDto> esimApiDtoList = new ArrayList<>();

        for(Map<String,Object> map: maps){
            EsimApiDto esimApiDto = new EsimApiDto();
            esimApiDto.setProductId(Objects.toString(map.get("product_id"), ""));
            esimApiDto.setDescription(Objects.toString(map.get("description"), ""));
            esimApiDto.setDays(Objects.toString(map.get("days"), ""));
            esimApiDto.setApiPurchasePrice(Objects.toString(map.get("apiPurchasePrice"), ""));
            esimApiDto.setApiPurchaseCurrency(Objects.toString(map.get("apiPurchaseCurrency"), ""));
            esimApiDtoList.add(esimApiDto);
        }
        SXSSFWorkbook wb = null;
        OutputStream stream = null;

        try {
            ExcelFile<EsimApiDto> excelFile = new ExcelFile<>();
            wb = excelFile.renderExcel(esimApiDtoList, EsimApiDto.class, null);

            stream = response.getOutputStream();
            wb.write(stream);
            wb.dispose();
        } finally {
            wb.close();
            stream.close();
        }
    }
}
