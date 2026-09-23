package com.naver.naverspabackend.service.searchad.impl;

import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.dto.SearchAdShoppingHistDto;
import com.naver.naverspabackend.mybatis.mapper.SearchAdShoppingMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import com.naver.naverspabackend.service.searchad.SearchAdShoppingService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SearchAdShoppingServiceImpl implements SearchAdShoppingService {

    @Autowired
    private SearchAdShoppingMapper searchAdShoppingMapper;

    @Override
    public ApiResult<List<SearchAdShoppingDto>> fetchList(Map<String, Object> paramMap, PagingUtil page) {
        CommonUtil.setPageIntoMap(paramMap, page, searchAdShoppingMapper.selectListCnt(paramMap));
        return ApiResult.succeed(searchAdShoppingMapper.selectList(paramMap), page);
    }

    @Override
    public ApiResult<SearchAdShoppingDto> fetch(Map<String, Object> paramMap) {
        return ApiResult.succeed(searchAdShoppingMapper.selectDetail(paramMap), null);
    }

    @Override
    public ApiResult<?> create(Map<String, Object> paramMap) {
        if (!StringUtils.hasText(asString(paramMap.get("nccAdId")))) {
            return ApiResult.failed("nccAdId(소재 ID)는 필수입니다.", ResponseCode.ABNORMAL);
        }
        Map<String, Object> dup = new HashMap<String, Object>();
        dup.put("nccAdId", paramMap.get("nccAdId"));
        if (searchAdShoppingMapper.selectListCnt(dup) > 0) {
            return ApiResult.failed("이미 등록된 nccAdId 입니다.", ResponseCode.DUPLICATE_RESOURCE);
        }
        if (paramMap.get("useYn") == null || !StringUtils.hasText(asString(paramMap.get("useYn")))) {
            paramMap.put("useYn", "Y");
        }
        searchAdShoppingMapper.insert(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> update(Map<String, Object> paramMap) {
        if (paramMap.get("id") == null) {
            return ApiResult.failed("id는 필수입니다.", ResponseCode.ABNORMAL);
        }
        SearchAdShoppingDto existing = searchAdShoppingMapper.selectDetail(paramMap);
        if (existing == null) {
            return ApiResult.failed("대상을 찾을 수 없습니다.", ResponseCode.NOT_EXIST);
        }
        searchAdShoppingMapper.update(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<?> delete(Map<String, Object> paramMap) {
        if (paramMap.get("id") == null) {
            return ApiResult.failed("id는 필수입니다.", ResponseCode.ABNORMAL);
        }
        searchAdShoppingMapper.delete(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public List<SearchAdShoppingDto> findActiveList() {
        return searchAdShoppingMapper.selectActiveList();
    }

    @Override
    public ApiResult<List<SearchAdShoppingHistDto>> fetchHistList(Map<String, Object> paramMap) {
        if (paramMap.get("shoppingId") == null && paramMap.get("id") != null) {
            paramMap.put("shoppingId", paramMap.get("id"));
        }
        if (paramMap.get("shoppingId") == null) {
            return (ApiResult) ApiResult.failed("shoppingId는 필수입니다.", ResponseCode.ABNORMAL);
        }
        if (paramMap.get("limit") == null) {
            paramMap.put("limit", 50);
        }
        return ApiResult.succeed(searchAdShoppingMapper.selectHistList(paramMap), null);
    }

    @Override
    public void updateBidSnapshot(Map<String, Object> paramMap) {
        searchAdShoppingMapper.updateBidStatus(paramMap);
    }

    @Override
    public void insertBidHist(Map<String, Object> paramMap) {
        searchAdShoppingMapper.insertHist(paramMap);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
