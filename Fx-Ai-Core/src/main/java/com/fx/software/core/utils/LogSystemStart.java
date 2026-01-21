package com.fx.software.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @FileName LogSystemStart
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class LogSystemStart {
    private static final Logger log = LoggerFactory.getLogger(LogSystemStart.class);

    public LogSystemStart() {
    }

    public static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String sslkey = "server.ssl.key-store";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }

        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException var7) {
            log.warn("无法获取到对外地址，请检查网络情况。应用降级为仅使用localhost作为监听地址。此时，将拒绝一切远程访问！");
        }

        log.info("\n----------------------------------------------------------\n\t" +
                "应用 '{}' 已启动！ 访问地址:\n\t本地地址: \t\t{}://localhost:{}{}\n\t外部地址: \t\t{}://{}:{}{}\n\tProfile(s): \t{}\n--" +
                "--------------------------------------------------------",
                new Object[]{env.getProperty("spring.application.name"), protocol, serverPort, contextPath, protocol,
                        hostAddress, serverPort, contextPath, env.getActiveProfiles()});
    }
}
