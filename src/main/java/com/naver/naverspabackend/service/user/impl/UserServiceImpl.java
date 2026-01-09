package com.naver.naverspabackend.service.user.impl;

import com.naver.naverspabackend.dto.StoreDto;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.dto.UserStoreMappDto;
import com.naver.naverspabackend.enums.UserAuthority;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.mybatis.mapper.StoreMapper;
import com.naver.naverspabackend.mybatis.mapper.UserMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.service.user.UserService;
import com.naver.naverspabackend.util.CommonUtil;
import com.naver.naverspabackend.util.JsonUtil;
import com.naver.naverspabackend.util.PagingUtil;
import com.naver.naverspabackend.util.SecurityUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    private final StoreMapper storeMapper;


    public ApiResult<?>  deleteUser(UserDto entity) {
        int result =  userMapper.deleteUser(entity);

        return ApiResult.succeed(null, null);
    }

    public ApiResult<?>  createUser(UserDto entity) {
        Map<String, String> map = new HashMap<>();
        map.put("TYPE", entity.getUserAuthority().toString());
        map.put("PASSWORD", passwordEncoder.encode(entity.getPassword()));
        List<Map<String, String>> mapList = new ArrayList<>();
        mapList.add(map);
        String jsonPassword = JsonUtil.toJson(mapList);
        entity.setPassword(jsonPassword);
        int result =  userMapper.createUser(entity);

        List<UserStoreMappDto> userStoreMappDtoList = entity.getUserStoreMappDtoList();

        for(UserStoreMappDto userStoreMappDto : userStoreMappDtoList){

            UserDto userDto = new UserDto(entity.getEmail());
            userStoreMappDto.setUserDto(userDto);
            userMapper.createUserStoreMapp(userStoreMappDto);
        }

        return ApiResult.succeed(null, null);
    }


    public ApiResult<?>  updateUser(UserDto entity) {
        if(entity.getPassword()!=null && !entity.getPassword().trim().equals("")){
            Map<String, String> map = new HashMap<>();
            map.put("TYPE", entity.getUserAuthority().toString());
            map.put("PASSWORD", passwordEncoder.encode(entity.getPassword()));
            List<Map<String, String>> mapList = new ArrayList<>();
            mapList.add(map);
            String jsonPassword = JsonUtil.toJson(mapList);
            entity.setPassword(jsonPassword);
        }
        int result =  userMapper.updateUser(entity);

        userMapper.deleteUserStoreMapp(entity.getEmail());
        List<UserStoreMappDto> userStoreMappDtoList = entity.getUserStoreMappDtoList();

        for(UserStoreMappDto userStoreMappDto : userStoreMappDtoList){

            UserDto userDto = new UserDto(entity.getEmail());
            userStoreMappDto.setUserDto(userDto);
            userMapper.createUserStoreMapp(userStoreMappDto);
        }

        return ApiResult.succeed(null, null);
    }


    public UserDto getMyInfo() {
        Optional<UserDto> findUser = userMapper.findById(SecurityUtil.getCurrentUserEmail());
        if (findUser.isPresent()) {
            return findUser.get();
        } else {
            log.error("로그인 정보가 존재하지 않습니다.");
            throw new CustomException("로그인 정보가 존재하지 않습니다.");
        }
    }

    public boolean isValidStateForRegisterId(UserDto user){
        UserDto userByEmail = findUserByEmail(user.getEmail());
        return userByEmail == null ? true : false;
    }

    @Override
    public UserDto findUserByEmail(String email) {
        Optional<UserDto> findUser = userMapper.findById(email);
        return findUser.orElse(null);
    }

    @Override
    public ApiResult<List<UserDto>> selectUserList(Map<String, Object> paramMap, PagingUtil page) {
        CommonUtil.setPageIntoMap(paramMap, page, userMapper.selectUserListCnt(paramMap));
        List<UserDto> userDtoList = userMapper.selectUserList(paramMap);
        for(UserDto userDto : userDtoList){

            List<Map<String,String>> userStoreMappList = userMapper.findUserStoreMappByEmail(userDto.getEmail());
            List<UserStoreMappDto> userStoreMappDtoList = new ArrayList<>();
            for(Map<String,String> userStoreMapp : userStoreMappList){
                UserStoreMappDto userStoreMappDto = new UserStoreMappDto();
                Map<String, Object> storeParamMap = new HashMap<>();
                storeParamMap.put("id",userStoreMapp.get("id"));
                userStoreMappDto.setStoreDto(storeMapper.selectStoreDetail(storeParamMap));
                userStoreMappDtoList.add(userStoreMappDto);
            }
            userDto.setUserStoreMappDtoList(userStoreMappDtoList);
        }

        return ApiResult.succeed(userMapper.selectUserList(paramMap), page);
    }
}
