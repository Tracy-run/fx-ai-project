package com.fx.software.datasource.utils;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.fx.software.datasource.config.SlowSqlFilter;
import com.fx.software.datasource.config.SqlLogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @FileName DatasourceUtils
 * @Description
 * @Author fx
 * @date 2026-01-14
 */
public class DatasourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DatasourceUtils.class);
    private static final String ORACLE_TEST_SQL = "select 1 from dual";
    private static final String MYSQL_TEST_SQL = "select 1";

    public DatasourceUtils() {
    }

    public static void setOracleCommonParameter(DruidDataSource druidDataSource) {
        String url = druidDataSource.getUrl();
        log.info("url  === " + url);
        if (url != null && url.contains("jdbc:aoe:oracle")) {
            druidDataSource.setDriverClassName("com.ciphergateway.aoe.plugin.engine.AOEDriver");
        }

        setCommonParameter(druidDataSource);
        druidDataSource.setValidationQuery("select 1 from dual");
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMaxOpenPreparedStatements(80);
        setCommonFilter(druidDataSource);
    }

    public static void setOracleCommonParameterStatementFalse(DruidDataSource druidDataSource) {
        String url = druidDataSource.getUrl();
        log.info("url===" + url);
        if (url != null && url.contains("jdbc:aoe:oracle")) {
            druidDataSource.setDriverClassName("com.ciphergateway.aoe.plugin.engine.AOEDriver");
        }

        setCommonParameter(druidDataSource);
        druidDataSource.setValidationQuery("select 1 from dual");
        druidDataSource.setPoolPreparedStatements(false);
        druidDataSource.setMaxOpenPreparedStatements(-1);
        setCommonFilter(druidDataSource);
    }

    public static void setMySqlCommonParameter(DruidDataSource druidDataSource) {
        setCommonParameter(druidDataSource);
        druidDataSource.setValidationQuery("select 1");
        setCommonFilter(druidDataSource);
    }

    private static void setCommonFilter(DruidDataSource druidDataSource) {
        List<Filter> fList = new ArrayList();
        fList.add(new SlowSqlFilter(1500, 1200));
        fList.add(new SqlLogFilter());
        druidDataSource.setProxyFilters(fList);
    }

    private static void setCommonParameter(DruidDataSource druidDataSource) {
        druidDataSource.setInitialSize(3);
        druidDataSource.setMaxWait(10000L);
        druidDataSource.setTimeBetweenEvictionRunsMillis(2000L);
        druidDataSource.setMinEvictableIdleTimeMillis(300000L);
        druidDataSource.setMaxEvictableIdleTimeMillis(900000L);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(true);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setKeepAlive(false);
        druidDataSource.setPoolPreparedStatements(false);
        druidDataSource.setTimeBetweenLogStatsMillis(300000L);
    }





}
