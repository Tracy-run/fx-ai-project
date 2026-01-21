package com.fx.software.demo.config;


import com.fx.software.demo.mapper.ServiceMapConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fx")
@Slf4j
public class ConfigCoreMap {

    @Autowired
    ServiceMapConfigMapper serviceMapConfigMapper;

    /**
     * Server的详细设置
     */
    List<ServiceMapConfig> server;

    /**
     * 是否支持多Server
     */
    boolean enableMultiServer;

    /**
     * 是否从数据库加载配置
     */
    boolean initConfigFromDb;


    public ServiceMapConfig searchServiceByName(String serviceName){
       return this.server.stream().filter(a -> a.getName().equals(serviceName)).findFirst().orElse(null);
    }

    public void initServerConfig(){
        log.info("是否从数据库加载配置:" + initConfigFromDb);
        if (initConfigFromDb) {
            server = serviceMapConfigMapper.selectByMapServer(new ServiceMapConfig());
            log.info("加载mapServerConfigs=" + server);
        }
    }
}
