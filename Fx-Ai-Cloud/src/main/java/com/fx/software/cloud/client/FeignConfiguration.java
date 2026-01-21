package com.fx.software.cloud.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Logger.Level;

/**
 * @FileName FeignConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class FeignConfiguration {

    @Bean
    Level feignLoggerLevel() {
        return Level.BASIC;
    }
}
