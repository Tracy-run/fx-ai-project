package com.fx.software.es;

import com.fx.software.core.utils.LogSystemStart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;

/**
 * @FileName ESDataSynApp
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class ESDataSynApp {


    private final Environment env;

    private final String profile_dev = "dev";
    private final String profile_prod = "prod";

    public ESDataSynApp(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(this.profile_dev) && activeProfiles.contains(this.profile_prod)) {
            log.error("应用不允许同时运行dev和prod两个Profile，他们是互斥的，请检查启动命令");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ESDataSynApp.class);
        Environment env = app.run(args).getEnvironment();
        LogSystemStart.logApplicationStartup(env);

    }
}
