package com.naver.naverspabackend.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestContextHolderCustom {

    /**
     * 현재 servletContext를 반환한다.
     * @return
     */
    public static HttpServletRequest getCurrentServletRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

}
