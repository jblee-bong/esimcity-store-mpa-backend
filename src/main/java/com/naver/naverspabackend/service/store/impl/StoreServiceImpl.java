package com.naver.naverspabackend.service.store.impl;

import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.PagingUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public ApiResult<List<StoreDto>> fetchStoreList(Map<String, Object> paramMap, PagingUtil page){
        CommonUtil.setPageIntoMap(paramMap, page, storeMapper.selectStoreListCnt(paramMap));
        return ApiResult.succeed(storeMapper.selectStoreList(paramMap), page);
    }

    @Override
    public ApiResult<StoreDto> fetchStore(Map<String, Object> paramMap) {
        return ApiResult.succeed(storeMapper.selectStoreDetail(paramMap), null);
    }


    @Override
    public ApiResult<Void> deleteStore(Map<String, Object> paramMap) {
        storeMapper.deleteStore(paramMap);
        return ApiResult.succeed(null, null);
    }

    @Override
    public ApiResult<Void> createStore(Map<String, Object> paramMap) {
        StoreDto storeDetail = storeMapper.selectStoreDetail(paramMap);
        if(storeDetail==null){

            storeMapper.insertStore(paramMap);
        }else{
            paramMap.put("id",storeDetail.getId());
            storeMapper.updateStore(paramMap);
        }
        return ApiResult.succeed(null, null);
    }

    @Override
    public StoreDto findById(Map<String, Object> data) {
        return storeMapper.selectStoreDetail(data);
    }

    @Override
    public List<StoreDto> selectStoreList(Map<String, Object> param) {
        return storeMapper.selectStoreList(param);
    }


    @Override
    public ApiResult<Void> updateStore(Map<String, Object> paramMap) {
        storeMapper.updateStore(paramMap);
        return ApiResult.succeed(null, null);
    }

}
