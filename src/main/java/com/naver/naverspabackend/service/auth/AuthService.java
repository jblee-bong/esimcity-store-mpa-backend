package com.naver.naverspabackend.service.auth;

import com.naver.naverspabackend.dto.TokenDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    Authentication authenticate(UsernamePasswordAuthenticationToken authenticationToken);

    TokenDto generateTokenDto(Authentication authentication);

}
