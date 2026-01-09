package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.store.StoreService;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;

    @RequestMapping("/store/fetchList")
    public ApiResult<List<StoreDto>> fetchStoreList(@RequestBody Map<String, Object> paramMap, @TokenUser UserDto userDto1, @PageResolver PagingUtil pagingUtil){
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());
        return storeService.fetchStoreList(paramMap, pagingUtil);
    }

    @RequestMapping("/store/fetch")
    public ApiResult<StoreDto> fetchStoreList(@RequestBody Map<String, Object> paramMap){
        return storeService.fetchStore(paramMap);
    }

    @RequestMapping("/store/create")
    public ApiResult<Void> createStore(@RequestBody Map<String, Object> paramMap){
        return storeService.createStore(paramMap);
    }


    @RequestMapping("/store/update")
    public ApiResult<Void> updateStore(@RequestBody Map<String, Object> paramMap){
        return storeService.updateStore(paramMap);
    }

    @RequestMapping("/store/delete")
    public ApiResult<Void> deleteStore(@RequestBody Map<String, Object> paramMap){
        return storeService.deleteStore(paramMap);
    }

}
