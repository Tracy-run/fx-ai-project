package com.fx.software.core.apidoc;

import com.fx.software.core.config.TinyConfiguration;
import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.Servlet;

/**
 * @FileName SwaggerAutoConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({ApiInfo.class, BeanValidatorPluginsConfiguration.class, Servlet.class, DispatcherServlet.class})
@EnableSwagger2
@Import({BeanValidatorPluginsConfiguration.class})
@EnableSwaggerBootstrapUI
public class SwaggerAutoConfiguration {

    @Value("${spring.application.name}")
    String applicationName;
    @Value("${swagger.enable:false}")
    private boolean swaggerEnable;

    @Autowired
    TinyConfiguration configuration;

    public SwaggerAutoConfiguration() {
    }

    @Bean
    public Docket createRestApi() {
        return (new Docket(DocumentationType.SWAGGER_2)).enable(this.swaggerEnable).apiInfo(this.apiInfo()).select().apis(RequestHandlerSelectors.basePackage(this.configuration.getApidoc().getApiBasePackage())).paths(PathSelectors.any()).build();
    }

    private ApiInfo apiInfo() {
        return (new ApiInfoBuilder()).title("微服务[" + this.applicationName + "]API文档").build();
    }
}
