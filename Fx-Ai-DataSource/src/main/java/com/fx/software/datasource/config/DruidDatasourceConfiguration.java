package com.fx.software.datasource.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.fx.software.datasource.utils.DatasourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @FileName DruidDatasourceConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-14
 */
//@Configuration
@Component
@ConfigurationProperties(
        prefix = "ai.datasource",
        ignoreInvalidFields = true,
        ignoreUnknownFields = true)
public class DruidDatasourceConfiguration {


    private static final Logger log = LoggerFactory.getLogger(DruidDatasourceConfiguration.class);
    private final String ORA_JDBC_KEYWORD = "jdbc:oracle";
    private final String MYSQL_JDBC_KEYWORD = "jdbc:mysql";
    private final String DM_JDBC_KEYWORD = "jdbc:dm";
    private final String AOE_JDBC_KEYWORD = "jdbc:aoe:oracle";
    @Value("${spring.application.name}")
    String app;
    private boolean enabled = false;
    private String url;
    private String username;
    private String password;
    private String name;
    private int maxActive;
    private int minIdle;
    private boolean readOnly = false;
    private int slowSqlMaxMillis = 1500;
    private int slowSqlAvgMillis = 1000;
    private boolean parameterStatement = true;

    public DruidDatasourceConfiguration() {
    }

    @Bean(
            name = {"dataSource"},
            destroyMethod = "close"
    )
    public DruidDataSource getDatasource() {
        log.info("Druid Datasource From Tinyframework正在生成，微应用:{}", this.app);
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(this.url);
        druidDataSource.setUsername(this.username);
        druidDataSource.setPassword(this.password);
        druidDataSource.setName(this.app + "-ds");
        druidDataSource.setMaxActive(this.maxActive);
        druidDataSource.setMinIdle(this.minIdle);
        druidDataSource.setDefaultReadOnly(this.readOnly);
        String var10000 = this.url;
        this.getClass();
        if (StringUtils.contains(var10000, "jdbc:oracle")) {
            if (this.parameterStatement) {
                DatasourceUtils.setOracleCommonParameter(druidDataSource);
            } else {
                DatasourceUtils.setOracleCommonParameterStatementFalse(druidDataSource);
            }
        } else {
            var10000 = this.url;
            this.getClass();
            if (StringUtils.contains(var10000, "jdbc:aoe:oracle")) {
                DatasourceUtils.setOracleCommonParameter(druidDataSource);
            } else {
                var10000 = this.url;
                this.getClass();
                if (StringUtils.contains(var10000, "jdbc:mysql")) {
                    DatasourceUtils.setMySqlCommonParameter(druidDataSource);
                } else {
                    var10000 = this.url;
                    this.getClass();
                    if (StringUtils.contains(var10000, "jdbc:dm")) {
                        DatasourceUtils.setOracleCommonParameter(druidDataSource);
                    } else {
                        DatasourceUtils.setMySqlCommonParameter(druidDataSource);
                    }
                }
            }
        }

        List<Filter> fList = new ArrayList();
        fList.add(new SlowSqlFilter(this.slowSqlMaxMillis, this.slowSqlAvgMillis));
        fList.add(new SqlLogFilter());
        druidDataSource.setProxyFilters(fList);
        log.info("Druid Datasource From Tinyframework已生成，微应用:{}", this.app);
        return druidDataSource;
    }

}
