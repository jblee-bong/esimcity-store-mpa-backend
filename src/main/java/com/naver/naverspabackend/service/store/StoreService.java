package com.naver.naverspabackend.service.store;

import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface StoreService {

    ApiResult<List<StoreDto>> fetchStoreList(Map<String, Object> paramMap, PagingUtil page);

    ApiResult<StoreDto> fetchStore(Map<String, Object> paramMap);

    ApiResult<Void> updateStore(Map<String, Object> paramMap);

    ApiResult<Void> deleteStore(Map<String, Object> paramMap);

    ApiResult<Void> createStore(Map<String, Object> paramMap);

    StoreDto findById(Map<String, Object> data);

    List<StoreDto> selectStoreList(Map<String, Object> param);
}
