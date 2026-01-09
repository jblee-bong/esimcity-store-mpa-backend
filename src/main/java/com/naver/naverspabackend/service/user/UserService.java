package com.naver.naverspabackend.service.user;


import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.List;
import java.util.Map;

public interface UserService {

    ApiResult<?> createUser(UserDto entity) ;

    UserDto getMyInfo();

    boolean isValidStateForRegisterId(UserDto user);

    UserDto findUserByEmail(String email);

    ApiResult<List<UserDto>> selectUserList(Map<String, Object> paramMap, PagingUtil page);

    ApiResult<?> deleteUser(UserDto userDto);

    ApiResult<?> updateUser(UserDto userDto);
}
