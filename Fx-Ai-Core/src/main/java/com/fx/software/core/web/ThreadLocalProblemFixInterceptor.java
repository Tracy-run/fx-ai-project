package com.fx.software.core.web;

import com.fx.software.core.utils.ThreadLocalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @FileName ThreadLocalProblemFixInterceptor
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class ThreadLocalProblemFixInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalProblemFixInterceptor.class);

    public ThreadLocalProblemFixInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (ThreadLocalUtils.isThreadLocalEmpty()) {
            return true;
        } else {
            log.error("当前Web容器线程存在ThreadLocal，请检查是否存在线程污染问题。请求URI为：{}  ThreadLocal中的值为：{}", request.getRequestURI(), ThreadLocalUtils.getAll());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtils.remove();
    }
}
