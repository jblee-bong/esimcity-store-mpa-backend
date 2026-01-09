package com.naver.naverspabackend.controller;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.user.UserService;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userManage")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RequestMapping("/userList")
    public ApiResult<?> selectUserList(@RequestBody Map<String, Object> paramMap, @PageResolver PagingUtil page){
        return userService.selectUserList(paramMap, page);
    }


    @RequestMapping("/createUser")
    public ApiResult<?> createUser(@RequestBody UserDto userDto){
        return userService.createUser(userDto);
    }
    @RequestMapping("/updateUser")
    public ApiResult<?> updateUser(@RequestBody UserDto userDto){
        return userService.updateUser(userDto);
    }

    @RequestMapping("/deleteUser")
    public ApiResult<?> deleteUser(@RequestBody UserDto userDto){
        return userService.deleteUser(userDto);
    }



    @RequestMapping("/myInfo")
    public ApiResult<?> selectMyInfo(){
        return ApiResult.succeed(userService.getMyInfo(), null);
    }

}
