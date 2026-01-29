package com.fx.software.es.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.stat.JdbcSqlStat;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @FileName SlowSqlFilter
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class SlowSqlFilter extends StatFilter {


    Marker slowSqlMarker = MarkerFactory.getMarker("慢SQL");

    public SlowSqlFilter() {

        //统计1s以上的SQL
        this.slowSqlMillis = 1000;
    }

    @Override
    protected void handleSlowSql(StatementProxy statementProxy) {

        JdbcSqlStat sqlStat = statementProxy.getSqlStat();
        //数据源名称
        String datasourceName = sqlStat.getName();
        //执行的SQL语句
        String sql = sqlStat.getSql();
        //最近执行的SQL参数
        String lastSlowParameters = sqlStat.getLastSlowParameters();

        //执行的最长时间
        long executeMillisMax = sqlStat.getExecuteMillisMax();
        //执行次数
        long executeCount = sqlStat.getExecuteCount();
        //执行时间总和
        long executeMillisTotal = sqlStat.getExecuteMillisTotal();
        //执行成功次数
        long executeSuccessCount = sqlStat.getExecuteSuccessCount();
        //执行失败次数
        long errorCount = sqlStat.getErrorCount();

        //平均耗时
        long averageMills = executeMillisTotal / executeCount;


        log.warn(this.slowSqlMarker, "数据源={}, SQL={}, SQL参数={}, 最长耗时={}ms, 平均耗时={}ms, " +
                        "执行次数={}, 成功次数={}, 失败次数={}, 累计执行总长={}ms",
                datasourceName, sql, lastSlowParameters, executeMillisMax, averageMills,
                executeCount, executeSuccessCount, errorCount, executeMillisTotal);

    }

}
