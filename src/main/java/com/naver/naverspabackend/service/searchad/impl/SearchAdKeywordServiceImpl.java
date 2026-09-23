package com.naver.naverspabackend.service.searchad.impl;

import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.dto.SearchAdKeywordHistDto;
import com.naver.naverspabackend.mybatis.mapper.SearchAdKeywordMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.searchad.SearchAdKeywordService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SearchAdKeywordServiceImpl implements SearchAdKeywordService {

    @Autowired
    private SearchAdKeywordMapper searchAdKeywordMapper;

    @Override
    public ApiResult<List<SearchAdKeywordDto>> fetchList(Map<String, Object> paramMap, PagingUtil page) {
        CommonUtil.setPageIntoMap(paramMap, page, searchAdKeywordMapper.selectListCnt(paramMap));
        return ApiResult.succeed(searchAdKeywordMapper.selectList(paramMap), page);
    }

    @Override
    public ApiResult<SearchAdKeywordDto> fetch(Map<String, Object> paramMap) {
        return ApiResult.succeed(searchAdKeywordMapper.selectDetail(paramMap), null);
    }

    @Override
    public ApiResult<?> create(Map<String, Object> paramMap) {
        if (!StringUtils.hasText(asString(paramMap.get("nccKeywordId")))
            || !StringUtils.hasText(asString(paramMap.get("keyword")))) {
            return ApiResult.failed("nccKeywordId와 keyword는 필수입니다.", ResponseCode.ABNORMAL);
        }
        Map<String, Object> dup = new HashMap<String, Object>();
        dup.put("nccKeywordId", paramMap.get("nccKeywordId"));
        if (searchAdKeywordMapper.selectListCnt(dup) > 0) {
            return ApiResult.failed("이미 등록된 nccKeywordId 입니다.", ResponseCode.DUPLICATE_RESOURCE);
        }
        if (paramMap.get("useYn") == null || !StringUtils.hasText(asString(paramMap.get("useYn")))) {
            paramMap.put("useYn", "Y");
        }
        searchAdKeywordMapper.insert(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> update(Map<String, Object> paramMap) {
        if (paramMap.get("id") == null) {
            return ApiResult.failed("id는 필수입니다.", ResponseCode.ABNORMAL);
        }
        SearchAdKeywordDto existing = searchAdKeywordMapper.selectDetail(paramMap);
        if (existing == null) {
            return ApiResult.failed("대상을 찾을 수 없습니다.", ResponseCode.NOT_EXIST);
        }
        searchAdKeywordMapper.update(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> delete(Map<String, Object> paramMap) {
        if (paramMap.get("id") == null) {
            return ApiResult.failed("id는 필수입니다.", ResponseCode.ABNORMAL);
        }
        searchAdKeywordMapper.delete(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public List<SearchAdKeywordDto> findActiveList() {
        return searchAdKeywordMapper.selectActiveList();
    }

    @Override
    public ApiResult<List<SearchAdKeywordHistDto>> fetchHistList(Map<String, Object> paramMap) {
        if (paramMap.get("keywordId") == null && paramMap.get("id") != null) {
            paramMap.put("keywordId", paramMap.get("id"));
        }
        if (paramMap.get("keywordId") == null) {
            return (ApiResult) ApiResult.failed("keywordId는 필수입니다.", ResponseCode.ABNORMAL);
        }
        if (paramMap.get("limit") == null) {
            paramMap.put("limit", 50);
        }
        return ApiResult.succeed(searchAdKeywordMapper.selectHistList(paramMap), null);
    }

    @Override
    public void updateBidSnapshot(Map<String, Object> paramMap) {
        searchAdKeywordMapper.updateBidStatus(paramMap);
    }

    @Override
    public void insertBidHist(Map<String, Object> paramMap) {
        searchAdKeywordMapper.insertHist(paramMap);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
