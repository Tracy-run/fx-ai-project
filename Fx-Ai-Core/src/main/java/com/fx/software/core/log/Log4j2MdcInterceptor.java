package com.fx.software.core.log;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @FileName Log4j2MdcInterceptor
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class Log4j2MdcInterceptor extends HandlerInterceptorAdapter {

    public Log4j2MdcInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String useraccount = request.getHeader("TINY-AUTH-USERACCOUNT");
        if (StringUtils.isNoneEmpty(new CharSequence[]{useraccount})) {
            MDC.put("useraccount", useraccount);
        }

        String requestId = request.getHeader("TINY-REQUEST-ID");
        if (StringUtils.isNoneEmpty(new CharSequence[]{requestId})) {
            MDC.put("requestId", requestId);
        }

        return true;
    }

}
