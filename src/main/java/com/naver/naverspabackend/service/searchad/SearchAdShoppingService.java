package com.naver.naverspabackend.service.searchad;

import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.dto.SearchAdShoppingHistDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface SearchAdShoppingService {

    ApiResult<List<SearchAdShoppingDto>> fetchList(Map<String, Object> paramMap, PagingUtil page);

    ApiResult<SearchAdShoppingDto> fetch(Map<String, Object> paramMap);

    ApiResult<?> create(Map<String, Object> paramMap);

    ApiResult<?> update(Map<String, Object> paramMap);

    ApiResult<?> delete(Map<String, Object> paramMap);

    ApiResult<List<SearchAdShoppingHistDto>> fetchHistList(Map<String, Object> paramMap);

    List<SearchAdShoppingDto> findActiveList();

    void updateBidSnapshot(Map<String, Object> paramMap);

    void insertBidHist(Map<String, Object> paramMap);
}
