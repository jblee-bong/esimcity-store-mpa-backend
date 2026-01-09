package com.naver.naverspabackend.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naver.naverspabackend.response.ApiResult;
import com.naver.naverspabackend.response.model.ResponseCode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할때 401
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), ApiResult.failed("fail", ResponseCode.COUNTERFEIT));
    }
}
