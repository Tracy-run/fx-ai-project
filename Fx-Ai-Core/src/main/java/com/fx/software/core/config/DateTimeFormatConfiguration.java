package com.fx.software.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @FileName DateTimeFormatConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class DateTimeFormatConfiguration implements WebMvcConfigurer {

    public DateTimeFormatConfiguration() {
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }
}
