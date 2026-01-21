package com.fx.software.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @FileName ApplicationLifeConfiguration
 * @Description  服务启动配置加载
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class ApplicationLifeConfiguration {
    public ApplicationLifeConfiguration() {
    }

    @Bean
    public WebStartedAndPrintSystemInfo starting() {
        return new WebStartedAndPrintSystemInfo();
    }
}
