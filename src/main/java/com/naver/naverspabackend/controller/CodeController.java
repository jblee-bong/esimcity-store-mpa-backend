package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.CodeDto;
import com.naver.naverspabackend.dto.ProductDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.code.CodeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/code")
@AllArgsConstructor
public class CodeController {

    private final CodeService codeService;





    @RequestMapping(value = "/code-all-list")
    public ApiResult<List<CodeDto>> getCodeList(){
        return ApiResult.succeed(codeService.getCodeAll(), null);
    }

    @RequestMapping(value = "/code-seller-all-list")
    public ApiResult<List<ProductDto>> getCodeSellerList( @TokenUser UserDto userDto1){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("email",userDto1.getEmail());
        paramMap.put("userAuthority",userDto1.getUserAuthority().name());
        return codeService.getSellerCodeAll(paramMap);
    }

    @RequestMapping(value = "/insert")
    public ApiResult<?> insert(@RequestBody Map<String, Object> paramMap){
        return codeService.createCode(paramMap);
    }

    @RequestMapping(value = "/update")
    public ApiResult<?> update(@RequestBody Map<String, Object> paramMap){
        return codeService.updateCode(paramMap);
    }

    @RequestMapping(value = "/delete")
    public ApiResult<?> delete(@RequestBody Map<String, Object> paramMap){
        return codeService.deleteCode(paramMap);
    }

    @RequestMapping(value = "/fetchList")
    public ApiResult<List<CodeDto>> fetchList(@RequestBody Map<String, Object> paramMap){
        return codeService.fetchList(paramMap);
    }

    @RequestMapping(value = "/fetch")
    public ApiResult<CodeDto> fetch(@RequestBody Map<String, Object> paramMap){
        return codeService.fetch(paramMap);
    }
}
