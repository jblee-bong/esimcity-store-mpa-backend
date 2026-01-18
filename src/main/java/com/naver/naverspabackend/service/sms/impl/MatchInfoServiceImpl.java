package com.naver.naverspabackend.service.sms.impl;

import com.naver.naverspabackend.dto.ApiPurchaseItemDto;
import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.enums.ApiType;
import com.naver.naverspabackend.mybatis.mapper.ApiPurchaseItemMapper;
import com.naver.naverspabackend.mybatis.mapper.BulkMapper;
import com.naver.naverspabackend.mybatis.mapper.MatchInfoMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.sms.MatchInfoService;
import com.naver.naverspabackend.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchInfoServiceImpl implements MatchInfoService {

    @Autowired
    private MatchInfoMapper matchInfoMapper;

    @Autowired
    private ApiPurchaseItemMapper apiPurchaseItemMapper;
    @Autowired
    private BulkMapper bulkMapper;
    @Override
    public ApiResult<?> inserMatchInfo(Map<String, Object> matchInfoMap) throws Exception {

        List<Object> objectList = (List<Object>) matchInfoMap.get("optionIdList");
        List<Long> optionIdList = new ArrayList<>();
        for(Object object:objectList){
            optionIdList.add(Long.parseLong(object.toString()));
        }

        try {
            if(optionIdList != null){
                long timeStamp = (new Date()).getTime();
                matchInfoMap.put("timeStamp", timeStamp);
                for (Long i : optionIdList) {
                    matchInfoMap.put("optionId", i);
                    matchInfoMapper.deleteMatchInfo(matchInfoMap);
                    matchInfoMapper.inserMatchInfo(matchInfoMap);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> fetchMatchInfoList(Map<String, Object> paramMap, PagingUtil pagingUtil) {
        CommonUtil.setPageIntoMap(paramMap, pagingUtil, matchInfoMapper.adSelectMatchInfoListCnt(paramMap));
        return ApiResult.succeed(matchInfoMapper.adSelectMatchInfoList(paramMap), pagingUtil);
    }


    @Override
    public List<MatchInfoDto> fetchMatchInfoListForExcel(Map<String, Object> map) {
        List<MatchInfoDto> resultWithMainIdList = matchInfoMapper.adSelectMatchInfoListForExcel(map);
        /*for(MatchInfoDto matchInfoDto: resultWithMainIdList){

            String resultTxt = "";
            String deter = "";
            if (matchInfoDto.getOptionName1()!=null && matchInfoDto.getOptionName1() != "") {

                matchInfoDto.setOptionName1(matchInfoDto.getOptionName1());
                resultTxt += deter;
                resultTxt += matchInfoDto.getOptionName1();
                deter = " / ";
            }
            if (matchInfoDto.getOptionName2()!=null && matchInfoDto.getOptionName2() != "") {

                matchInfoDto.setOptionName2(matchInfoDto.getOptionName2());
                resultTxt += deter;
                resultTxt += matchInfoDto.getOptionName2();
                deter = " / ";
            }
            if (matchInfoDto.getOptionName3()!=null && matchInfoDto.getOptionName3() != "") {

                matchInfoDto.setOptionName3(matchInfoDto.getOptionName3());
                resultTxt += deter;
                resultTxt += matchInfoDto.getOptionName3();
                deter = " / ";
            }
            if (matchInfoDto.getOptionName4()!=null && matchInfoDto.getOptionName4() != "") {

                matchInfoDto.setOptionName4(matchInfoDto.getOptionName4());
                resultTxt += deter;
                resultTxt += matchInfoDto.getOptionName4();
                deter = " / ";
            }

            matchInfoDto.setOptionName(resultTxt);
            matchInfoDto.setCreateDt(matchInfoDto.getRegistDt());
        }*/


        return resultWithMainIdList;
    }

    @Override
    public ApiResult<?> fetchListExcelUpload(List<MatchInfoDto> matchInfoDtoList) {
        for(MatchInfoDto matchInfoDto:matchInfoDtoList){
            matchInfoMapper.updateMatchInfoForUpload(matchInfoDto);
        }
        return ApiResult.succeed(null, null);
    }

    @Override
    public List<MatchInfoDto> selectMatchInfoListAll(MatchInfoDto matchInfoDto) {
        return matchInfoMapper.selectMatchInfoListAll(matchInfoDto);
    }

    @Override
    public ApiResult<?> fetchMatchInfo(Map<String, Object> paramMap) {
        // main
        MatchInfoDto mainMatchInfoDto = matchInfoMapper.fetchMatchInfo(paramMap);

        // 그룹 을하려면 아래걸로 사용
        //List<MatchInfoDto> matchInfoDtosRelMainId = matchInfoMapper.fetchMatchInfoListMainId(paramMap);
        //단일로 사용
        List<MatchInfoDto> matchInfoDtosRelMainId = new ArrayList<>();
        matchInfoDtosRelMainId.add(mainMatchInfoDto);

        List<Long> disableOptionList = matchInfoMapper.disableOptionList(paramMap);

        Map<String, Object> matchInfoMap = new HashMap<>();

        matchInfoMap.put("mainMatchInfoDto", mainMatchInfoDto);
        matchInfoMap.put("matchInfoDtosRelMainId", matchInfoDtosRelMainId);
        matchInfoMap.put("disableOptionList", disableOptionList);

        return ApiResult.succeed(matchInfoMap, null);
    }

    @Override
    public ApiResult<?> updateMatchInfo(Map<String, Object> paramMap) {
        // 기존 matchInfo 조건 비교 후 삽입 or 삭제
        List<Object> objectList = (List<Object>) paramMap.get("optionIdList");
        List<Object> preObjectList = (List<Object>) paramMap.get("preList");
        List<Long> optionIdList = new ArrayList<>();
        for(Object object:objectList){
            optionIdList.add(Long.parseLong(object.toString()));
        }
        List<Long> preList = new ArrayList<>();
        for(Object object:preObjectList){
            preList.add(Long.parseLong(object.toString()));
        }

        Long mainId = (Long) paramMap.get("mainId");
        paramMap.put("timeStamp", mainId);

        try {
            if(optionIdList != null){
                for (Long i : optionIdList) {
                    paramMap.put("optionId", i);
                    int index = preList.indexOf(i);
                    if(index > -1){
                        matchInfoMapper.updateMatchInfo(paramMap);
                        preList.remove(index);
                    }else{
                        matchInfoMapper.inserMatchInfo(paramMap);
                    }
                }
                for(Long i: preList){
                    paramMap.put("optionId", i);
                    matchInfoMapper.deleteMatchInfo(paramMap);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> deleteMatchInfo(Map<String, Object> paramMap) {
        return ApiResult.succeed(matchInfoMapper.deleteMatchInfoByMainId(paramMap), null);
    }

    @Override
    public ApiResult<?> fetchEsimProductIdList(Map<String, Object> paramMap) throws Exception {
        if(paramMap.get("value").equals("01"))
            return ApiResult.succeed(Tel25Util.contextLoads1(), null);
        else if(paramMap.get("value").equals("02")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.NIZ.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("03")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.TSIM.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("04")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.AIRALO.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("05")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.TUGE.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("06")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.WORLDMOVE.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("07")){
            return ApiResult.succeed(apiPurchaseItemMapper.selectApiPurchaseItemWithApiPurchaseItemType(new ApiPurchaseItemDto(ApiType.ESIMACCESS.name(), paramMap.get("searchKey")!=null?paramMap.get("searchKey").toString():"")), null);
        }else if(paramMap.get("value").equals("99")){
            return ApiResult.succeed(BulkUtil.contextLoads1(), null);
        }else{
            return null;
        }
    }

}
