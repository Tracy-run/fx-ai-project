package com.fx.software.core.config;

import com.fx.software.core.log.Log4j2MdcInterceptor;
import com.fx.software.core.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @FileName WebMvcConfg
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class WebMvcConfg implements WebMvcConfigurer {

    TinyConfiguration tinyConfiguration;

    public WebMvcConfg() {
    }

    @Bean
    Log4j2MdcInterceptor log4j2MDCInterceptor() {
        return new Log4j2MdcInterceptor();
    }

    @Bean
    JwtFilter jwtFilter() {
        return new JwtFilter(this.tinyConfiguration);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.jwtFilter()).excludePathPatterns(new String[]{"/actuator/**"});
        registry.addInterceptor(this.log4j2MDCInterceptor());
    }

}
