package com.fx.software.datasource.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @FileName SlowSqlFilter
 * @Description
 * @Author fx
 * @date 2026-01-14
 */
public class SlowSqlFilter extends StatFilter {

    private static final Logger log = LoggerFactory.getLogger(SlowSqlFilter.class);
    Marker slowSqlMarker = MarkerFactory.getMarker("慢SQL");
    int slowSqlMaxMillis;
    int slowSqlAvgMillis;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public SlowSqlFilter(int slowSqlMaxMillis, int slowSqlAvgMillis) {
        this.slowSqlMaxMillis = slowSqlMaxMillis;
        this.slowSqlAvgMillis = slowSqlAvgMillis;
        this.slowSqlMillis = slowSqlMaxMillis < slowSqlAvgMillis ? (long)slowSqlMaxMillis : (long)slowSqlAvgMillis;
    }

    protected void handleSlowSql(StatementProxy statementProxy) {
        JdbcSqlStat sqlStat = statementProxy.getSqlStat();
        String datasourceName = sqlStat.getName();
        String sql = sqlStat.getSql();
        String lastSlowParameters = sqlStat.getLastSlowParameters();
        long executeMillisMax = sqlStat.getExecuteMillisMax();
        long executeCount = sqlStat.getExecuteCount();
        long executeMillisTotal = sqlStat.getExecuteMillisTotal();
        long executeSuccessCount = sqlStat.getExecuteSuccessCount();
        long errorCount = sqlStat.getErrorCount();
        long averageMills = executeMillisMax;
        if (executeCount > 0L) {
            averageMills = executeMillisTotal / executeCount;
        }

        if (executeMillisMax > (long)this.slowSqlMaxMillis || averageMills > (long)this.slowSqlAvgMillis) {
            try {
                String valueAsString = objectMapper.writeValueAsString(sqlStat);
                log.warn(this.slowSqlMarker, valueAsString);
            } catch (JsonProcessingException var19) {
                var19.printStackTrace();
            }
        }

    }

}
