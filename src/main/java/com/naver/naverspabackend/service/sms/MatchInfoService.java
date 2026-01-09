package com.naver.naverspabackend.service.sms;

import com.naver.naverspabackend.dto.MatchInfoDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface MatchInfoService {

    ApiResult<?> inserMatchInfo(Map<String, Object> paramMapp) throws Exception;

    ApiResult<?> fetchMatchInfoList(Map<String, Object> paramMap, PagingUtil pagingUtil);

    ApiResult<?> fetchMatchInfo(Map<String, Object> paramMap);

    ApiResult<?> updateMatchInfo(Map<String, Object> paramMap);

    ApiResult<?> deleteMatchInfo(Map<String, Object> paramMap);

    ApiResult<?> fetchEsimProductIdList(Map<String, Object> paramMap) throws NoSuchAlgorithmException, Exception;

    List<MatchInfoDto> fetchMatchInfoListForExcel(Map<String, Object> map);

    ApiResult<?> fetchListExcelUpload(List<MatchInfoDto> matchInfoDtoList);

    List<MatchInfoDto> selectMatchInfoListAll(MatchInfoDto matchInfoDto);
}
