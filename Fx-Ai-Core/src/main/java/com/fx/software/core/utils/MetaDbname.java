package com.fx.software.core.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @FileName MetaDbname
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Data
public class MetaDbname implements Serializable {



    private String uuid;

    /**
     * 英文名
     */
    private String name;

    /**
     * 中文名
     */
    private String alias;

    /**
     * 读数据库类型
     */
    private String dbTypeR;
    /**
     * 读数据库名称
     */
    private String dbNameR;
    /**
     * 读数据库的IP地址
     */
    private String dbIpR;
    /**
     * 读数据库端口号
     */
    private String dbPortR;
    /**
     * 读数据库登录用户
     */
    private String dbUserR;
    /**
     * 读数据库登录密码
     */
    private String dbPasswordR;
    /**
     * 写数据库类型
     */
    private String dbTypeW;
    /**
     * 写数据库名称
     */
    private String dbNameW;
    /**
     * 写数据库的IP地址
     */
    private String dbIpW;
    /**
     * 写数据库端口号
     */
    private String dbPortW;
    /**
     * 写数据库登录用户
     */
    private String dbUserW;
    /**
     * 写数据库登录密码
     */
    private String dbPasswordW;

    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date creatTime;

    private String creator;

    private String modifier;

    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
    /**
     * 读数据源最小连接数
     */
    private String dbConnectionsMinR;
    /**
     * 读数据源最大连接数
     */
    private String dbConnectionsMaxR;
    /**
     * 写数据源最小连接数
     */
    private String dbConnectionsMinW;
    /**
     * 写数据源最大连接数
     */
    private String dbConnectionsMaxW;

    /**
     * 读数据数据库连接
     */
    private String dbR;

    /**
     * 写数据数据库连接
     */
    private String dbW;

    public MetaDbname(String uuid, String name, String alias, String dbTypeR, String dbNameR,
                      String dbIpR, String dbPortR, String dbUserR, String dbPasswordR, String dbTypeW,
                      String dbNameW, String dbIpW, String dbPortW, String dbUserW, String dbPasswordW,
                      Date creatTime, String creator, String modifier, Date modifyTime,
                      String dbConnectionsMinR, String dbConnectionsMaxR, String dbConnectionsMinW, String dbConnectionsMaxW,String dbR,String dbW) {
        this.uuid = uuid;
        this.name = name;
        this.alias = alias;
        this.dbTypeR = dbTypeR;
        this.dbNameR = dbNameR;
        this.dbIpR = dbIpR;
        this.dbPortR = dbPortR;
        this.dbUserR = dbUserR;
        this.dbPasswordR = dbPasswordR;
        this.dbTypeW = dbTypeW;
        this.dbNameW = dbNameW;
        this.dbIpW = dbIpW;
        this.dbPortW = dbPortW;
        this.dbUserW = dbUserW;
        this.dbPasswordW = dbPasswordW;
        this.creatTime = creatTime;
        this.creator = creator;
        this.modifier = modifier;
        this.modifyTime = modifyTime;

        this.dbConnectionsMinR = dbConnectionsMinR;
        this.dbConnectionsMaxR = dbConnectionsMaxR;
        this.dbConnectionsMinW = dbConnectionsMinW;
        this.dbConnectionsMaxW = dbConnectionsMaxW;
        this.dbR = dbR;
        this.dbW = dbW;
    }

    public MetaDbname() {
        super();
    }

}
