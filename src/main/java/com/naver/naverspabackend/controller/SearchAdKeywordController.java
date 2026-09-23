package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.SearchAdKeywordDto;
import com.naver.naverspabackend.dto.SearchAdKeywordHistDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.searchad.SearchAdKeywordService;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchAdKeywordController {

    private final SearchAdKeywordService searchAdKeywordService;

    @RequestMapping("/search-ad-keyword/fetchList")
    public ApiResult<List<SearchAdKeywordDto>> fetchList(
        @RequestBody Map<String, Object> paramMap,
        @TokenUser UserDto userDto,
        @PageResolver PagingUtil pagingUtil) {
        return searchAdKeywordService.fetchList(paramMap, pagingUtil);
    }

    @RequestMapping("/search-ad-keyword/fetch")
    public ApiResult<SearchAdKeywordDto> fetch(@RequestBody Map<String, Object> paramMap) {
        return searchAdKeywordService.fetch(paramMap);
    }

    @RequestMapping("/search-ad-keyword/fetchHistList")
    public ApiResult<List<SearchAdKeywordHistDto>> fetchHistList(@RequestBody Map<String, Object> paramMap) {
        return searchAdKeywordService.fetchHistList(paramMap);
    }

    @RequestMapping("/search-ad-keyword/create")
    public ApiResult<?> create(@RequestBody Map<String, Object> paramMap) {
        return searchAdKeywordService.create(paramMap);
    }

    @RequestMapping("/search-ad-keyword/update")
    public ApiResult<?> update(@RequestBody Map<String, Object> paramMap) {
        return searchAdKeywordService.update(paramMap);
    }

    @RequestMapping("/search-ad-keyword/delete")
    public ApiResult<?> delete(@RequestBody Map<String, Object> paramMap) {
        return searchAdKeywordService.delete(paramMap);
    }
}
