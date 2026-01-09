package com.naver.naverspabackend.resolver;

import com.naver.naverspabackend.annotation.PageResolver;
import com.naver.naverspabackend.util.PagingUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Log4j2
@Component
@RequiredArgsConstructor
public class PagingResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isPaging = parameter.getParameterAnnotation(PageResolver.class) != null;
        boolean isString = PagingUtil.class.equals(parameter.getParameterType());
        return isPaging && isString;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String page = Objects.toString(webRequest.getParameter("page"), "0");
        String size = Objects.toString(webRequest.getParameter("size"), "10");
        String sort = webRequest.getParameter("sort");

        PagingUtil build = PagingUtil.builder()
            .currentPageNo(Integer.valueOf(page) - 1)
            .recordCountPerPage(Integer.valueOf(size))
            .pageSize(10)
            .build();

        return build;
    }
}