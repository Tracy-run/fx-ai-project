package com.fx.software.core.config;

import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;


/**
 * @FileName SentinelConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class SentinelConfiguration {

    public SentinelConfiguration() {
    }

    // 在外部类的方法中定义匿名内部类，实现RequestOriginParser接口
    @PostConstruct
    public void initSentinel() {
        RequestOriginParser originParser = new RequestOriginParser() {
            // 实现parseOrigin方法，从请求头中获取"TINY-ORIGIN"作为来源标识
            @Override
            public String parseOrigin(HttpServletRequest request) {
                return request.getHeader("TINY-ORIGIN");
            }
        };
        // 通常会将该originParser设置到WebCallbackManager中
        // WebCallbackManager.setRequestOriginParser(originParser);
    }
}