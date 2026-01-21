package com.fx.software.core.config;

import com.fx.software.core.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @FileName WebStartedAndPrintSystemInfo
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class WebStartedAndPrintSystemInfo implements ApplicationListener<WebServerInitializedEvent> {

    public WebStartedAndPrintSystemInfo() {
    }
    private static final Logger log = LoggerFactory.getLogger(WebStartedAndPrintSystemInfo.class);

    @Value("${spring.application.name}")
    String applicationName;

    @Autowired
    TinyConfiguration tinyConfiguration;
    private final String DEBUG_USERACCOUNT = "root";
    private final String DEBUG_TENANTS = "Fx";

    @Override
    public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
        int port = webServerInitializedEvent.getWebServer().getPort();
        String debugToken = SecurityUtils.createDebugToken(this.tinyConfiguration.getSecurity().getJwt().getSecret(), "root", "inspur");
        log.info("--------------start print---------------------");
        log.info("FX Web容器已启动！应用名称={}, 端口={}", this.applicationName, port);
        log.info("Swagger文档地址为：{}", "http://localhost:" + port + "/v2/api-docs");
        log.info("应用检查端点地址为：{}", "http://localhost:" + port + "/actuator");
        log.info("系统调试Token为:{}", debugToken);
        log.info("--------------end print-------------------");
    }






}
