package com.fx.software.datasource.config;

import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @FileName SqlLogFilter
 * @Description
 * @Author fx
 * @date 2026-01-14
 */
public class SqlLogFilter extends Slf4jLogFilter {

    private static final Logger log = LoggerFactory.getLogger(SqlLogFilter.class);
    private final Marker druidStatementMarker = MarkerFactory.getMarker("DRUID_STATEMENT");
    private final Marker druidConnectMarker = MarkerFactory.getMarker("DRUID_CONNECT");
    private final Marker druidResultSetMarker = MarkerFactory.getMarker("DRUID_RESULTSET");
    private static ObjectMapper objectMapper = new ObjectMapper();

    public SqlLogFilter() {
        this.setConnectionLogErrorEnabled(false);
        this.setConnectionLogEnabled(false);
        this.setDataSourceLogEnabled(false);
        this.setStatementLogEnabled(true);
        this.setStatementLogErrorEnabled(true);
        this.setResultSetLogEnabled(false);
        this.setResultSetLogErrorEnabled(true);
    }

    protected void connectionLog(String message) {
        log.debug(this.druidConnectMarker, message);
    }

    protected void statementLog(String message) {
        log.debug(this.druidStatementMarker, message);
    }

    protected void resultSetLog(String message) {
        log.debug(this.druidResultSetMarker, message);
    }

    protected void resultSetLogError(String message, Throwable error) {
        log.error(this.druidResultSetMarker, message, error);
    }

    protected void statementLogError(String message, Throwable error) {
        log.error(this.druidStatementMarker, message, error);
    }

}
