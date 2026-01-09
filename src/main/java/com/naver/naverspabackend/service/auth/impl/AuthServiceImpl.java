package com.naver.naverspabackend.service.auth.impl;

import com.naver.naverspabackend.dto.TokenDto;
import com.naver.naverspabackend.security.token.TokenProvider;
import com.naver.naverspabackend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    @Override
    public Authentication authenticate(UsernamePasswordAuthenticationToken authenticationToken) {
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }

    @Override
    public TokenDto generateTokenDto(Authentication authentication) {
        return tokenProvider.generateTokenDto(authentication);
    }

}
