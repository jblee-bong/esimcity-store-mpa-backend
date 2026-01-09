package com.naver.naverspabackend.security;

import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.exception.CustomException;
import com.naver.naverspabackend.mybatis.mapper.UserMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws AuthenticationServiceException {
        Optional<UserDto> userDetails = userMapper.findById(email);
        if (!userDetails.isPresent()){
            throw new CustomException("존재하지 않는 사용자입니다.");
        }
        return userDetails.map(user -> createUserDetails(user)).get();
    }

    // DB 에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(UserDto user) {
        return new CustomUserDetails(user);
    }
}