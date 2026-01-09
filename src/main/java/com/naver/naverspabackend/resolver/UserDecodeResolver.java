package com.naver.naverspabackend.resolver;

import com.naver.naverspabackend.annotation.TokenUser;
import com.naver.naverspabackend.common.RequestHolder;
import com.naver.naverspabackend.common.RequestHolderKey;
import com.naver.naverspabackend.dto.UserDto;
import com.naver.naverspabackend.security.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserDecodeResolver implements HandlerMethodArgumentResolver {

    private final String AUTHORIZATION_KEY = "Authorization";

    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isTokenUser = parameter.getParameterAnnotation(TokenUser.class) != null;
        boolean isString = UserDto.class.equals(parameter.getParameterType());
        return isTokenUser && isString;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String authorizationHeader = webRequest.getHeader(AUTHORIZATION_KEY);
        log.info(authorizationHeader);
        if (authorizationHeader == null) {
            return null;
        }
        String jwtToken = authorizationHeader.substring(7);
        Authentication authentication = tokenProvider.getAuthentication(jwtToken);
        String userEmail = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().toString();

        RequestHolder.put(RequestHolderKey.USER_EMAIL, userEmail);

        return new UserDto(userEmail,role);
    }
}