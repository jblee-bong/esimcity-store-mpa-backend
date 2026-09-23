package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.SearchAdShoppingDto;
import com.naver.naverspabackend.dto.SearchAdShoppingHistDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.searchad.SearchAdShoppingService;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchAdShoppingController {

    private final SearchAdShoppingService searchAdShoppingService;

    @RequestMapping("/search-ad-shopping/fetchList")
    public ApiResult<List<SearchAdShoppingDto>> fetchList(
        @RequestBody Map<String, Object> paramMap,
        @TokenUser UserDto userDto,
        @PageResolver PagingUtil pagingUtil) {
        return searchAdShoppingService.fetchList(paramMap, pagingUtil);
    }

    @RequestMapping("/search-ad-shopping/fetch")
    public ApiResult<SearchAdShoppingDto> fetch(@RequestBody Map<String, Object> paramMap) {
        return searchAdShoppingService.fetch(paramMap);
    }

    @RequestMapping("/search-ad-shopping/fetchHistList")
    public ApiResult<List<SearchAdShoppingHistDto>> fetchHistList(@RequestBody Map<String, Object> paramMap) {
        return searchAdShoppingService.fetchHistList(paramMap);
    }

    @RequestMapping("/search-ad-shopping/create")
    public ApiResult<?> create(@RequestBody Map<String, Object> paramMap) {
        return searchAdShoppingService.create(paramMap);
    }

    @RequestMapping("/search-ad-shopping/update")
    public ApiResult<?> update(@RequestBody Map<String, Object> paramMap) {
        return searchAdShoppingService.update(paramMap);
    }

    @RequestMapping("/search-ad-shopping/delete")
    public ApiResult<?> delete(@RequestBody Map<String, Object> paramMap) {
        return searchAdShoppingService.delete(paramMap);
    }
}
