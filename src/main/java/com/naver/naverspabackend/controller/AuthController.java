package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.dto.TokenDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.dto.wrapper.TokenUserWrapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.auth.AuthBusinessLogic;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthBusinessLogic authBusinessLogic;

    @PostMapping("/admin/login")
    public ApiResult<TokenUserWrapper> adminLogin(@RequestBody UserDto userDto , @TokenUser UserDto userDto1, HttpServletResponse response) {
        return ApiResult.succeed(authBusinessLogic.adminLogin(userDto, response), null);
    }

    @PostMapping("/reissue")
    public ApiResult<TokenUserWrapper> reissue(@RequestBody TokenDto tokenRequestDto) {
        return ApiResult.succeed(authBusinessLogic.reissue(tokenRequestDto), null);
    }

    @PostMapping("/signup")
    public ApiResult<Void> signup(@RequestBody UserDto userDto) {
        authBusinessLogic.createMember(userDto);
        return ApiResult.succeed(null, "사용자 가입에 성공하였습니다.", null);
    }


}
