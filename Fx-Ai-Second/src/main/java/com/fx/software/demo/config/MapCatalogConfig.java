package com.fx.software.demo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @FileName MapCatalogConfig
 * @Description
 * @Author fx
 * @date 2025-09-21
 */
@Data
@Slf4j
public class MapCatalogConfig {




    /**
     * ArcGIS Server的HOST
     */
    String serverHost;

    /**
     * 端口
     */
    int serverPort;

    /**
     * 是否从数据库加载配置
     */
    boolean initConfigFromDb;





}
