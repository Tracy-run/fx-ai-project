package com.fx.software.demo.props;

import com.fx.software.demo.config.MapCatalogConfig;
import com.fx.software.demo.service.httpResolver.IHttpRequestResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MonkeywieCommond implements CommandLineRunner {

    @Value("${server.port:9999}")
    int port;

    @Autowired
    IHttpRequestResolver requestResolver;

    @Autowired
    MapCatalogConfig catalogConfig;

    @Override
    public void run(String... args) throws Exception {
        try {
            serviceDo();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void serviceDo() throws Exception{
        log.info("-----------代理服务已启动-----------");





    }




}
