package com.fx.software.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;

/**
 * @FileName WebConfigurer
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class WebConfigurer implements ServletContextInitializer, WebServerFactoryCustomizer<WebServerFactory> {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);
    private final Environment env;

    public WebConfigurer(Environment env) {
        this.env = env;
    }


    @Override
    public void customize(WebServerFactory factory) {
        this.setMimeMappings(factory);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if (this.env.getActiveProfiles().length != 0) {
            this.log.info("Web应用正在启动，启动使用的Profile为: {}", (Object[])this.env.getActiveProfiles());
        } else {
            this.log.info("Web应用使用缺省配置启动！");
        }
        this.log.info("Web应用配置完成！");
    }

    private void setMimeMappings(WebServerFactory server) {
        if (server instanceof ConfigurableServletWebServerFactory) {
            MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
            mappings.add("html", "text/html;charset=" + StandardCharsets.UTF_8.name().toLowerCase());
            mappings.add("json", "text/html;charset=" + StandardCharsets.UTF_8.name().toLowerCase());
            ConfigurableServletWebServerFactory servletWebServer = (ConfigurableServletWebServerFactory)server;
            servletWebServer.setMimeMappings(mappings);
        }

    }
}
