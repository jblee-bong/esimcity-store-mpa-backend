package com.naver.naverspabackend.service.searchad;

import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.dto.SearchAdKeywordHistDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface SearchAdKeywordService {

    ApiResult<List<SearchAdKeywordDto>> fetchList(Map<String, Object> paramMap, PagingUtil page);

    ApiResult<SearchAdKeywordDto> fetch(Map<String, Object> paramMap);

    ApiResult<?> create(Map<String, Object> paramMap);

    ApiResult<?> update(Map<String, Object> paramMap);

    ApiResult<?> delete(Map<String, Object> paramMap);

    ApiResult<List<SearchAdKeywordHistDto>> fetchHistList(Map<String, Object> paramMap);

    List<SearchAdKeywordDto> findActiveList();

    void updateBidSnapshot(Map<String, Object> paramMap);

    void insertBidHist(Map<String, Object> paramMap);
}
