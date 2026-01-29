package com.fx.software.es.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.fx.software.core.utils.MetaDbname;
import com.fx.software.core.utils.RedisConst;
import com.fx.software.core.utils.SqlUtil;
import com.fx.software.datasource.config.SqlLogFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;


/**
 * @FileName JdbcDatasource
 * @Description   动态生成数据源
 * @Author fx
 * @date 2026-01-29
 */
@Component
@Order(value = 7)
@Slf4j
public class JdbcDatasource {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    Map<String, DataSource> dataSourceMap;

    Map<String, JdbcTemplate> jdbcTemplateMap;

    Map<String, DataSourceTransactionManager> transactionManagerMap;

    public static final String READ = "READ";
    public static final String WRITE = "WRITE";

    @PostConstruct
    void init() {
        dataSourceMap = new LinkedCaseInsensitiveMap(10);
        jdbcTemplateMap = new LinkedCaseInsensitiveMap<>(10);
        transactionManagerMap = new LinkedCaseInsensitiveMap<>(10);

        //初始化数据源对象
        this.initDatasourceMap();

        //初始化JdbcTemplate对象
        this.initJdbcTemplateMap();
        //初始化事务管理器
        initTransactionManager();

        log.info("已经生成数据源:{}个， 生成JdbcTemplate:{}个", this.dataSourceMap.size(), this.jdbcTemplateMap.size());
    }

    private void initJdbcTemplateMap() {

        if (dataSourceMap!=null){

            dataSourceMap.forEach((dataSourceName,dataSource)->{
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplateMap.put(dataSourceName, jdbcTemplate);
            });

        }
    }

    private void initTransactionManager() {

        if (dataSourceMap!=null){

            dataSourceMap.forEach((dataSourceName,dataSource)->{

                DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);

                transactionManagerMap.put(dataSourceName, dataSourceTransactionManager);
            });

        }
    }

    private void initDatasourceMap() {

        try{
            //获取数据库信息，数据源配置信息暂存在Redis中，初始化时metainfo中数据库信息还未初始化，chongredis中获取
            Set<Object> keySet = this.redisTemplate.opsForHash().keys(RedisConst.DBNAME_KEY_PRE);

            if (keySet != null && !keySet.isEmpty()) {
                Map<String, MetaDbname> metaDbnameMap = new HashMap<String, MetaDbname>(10);
                for (Object key : keySet) {

                    MetaDbname metaDbname = (MetaDbname) this.redisTemplate.opsForHash().get(RedisConst.DBNAME_KEY_PRE, key);
                    if (null != metaDbname){
                        metaDbnameMap.put(metaDbname.getName(), metaDbname);
                    }
                }

                //数据源链接
                metaDbnameMap.forEach((key,metaDbname)->{

                    //只读数据源
                    DruidDataSource druidDataSourceRead = null;
                    //读写数据源
                    DruidDataSource druidDataSourceWrite = null;

                    boolean hasWriteConn = false;
                    boolean hasReadConn = false;



                    if(StringUtils.isNotEmpty(metaDbname.getDbNameW())) {
                        //如果写数据源是存在的
                        hasWriteConn = true;
                    }

                    if(StringUtils.isNotEmpty(metaDbname.getDbNameR())){
                        //如果只读连接是存在的
                        hasReadConn = true;
                    }

                    if( hasWriteConn && SqlUtil.DATABASE_ORACLE.equalsIgnoreCase(metaDbname.getDbTypeW())){
                        //处理生成Oracle写连接
                        druidDataSourceWrite = this.generateOracleDatasource(metaDbname, WRITE);
                    }

                    if(hasReadConn && SqlUtil.DATABASE_ORACLE.equalsIgnoreCase(metaDbname.getDbTypeR())){
                        //处理生成Oracle只读连接
                        druidDataSourceRead = this.generateOracleDatasource(metaDbname, READ);
                    } else if(!hasReadConn){
                        //没有只读连接，则使用读连接即写连接
                        druidDataSourceRead = druidDataSourceWrite;
                    }

//                    dataSourceMap.put(metaDbname.getName() + "_"+ READ, druidDataSourceRead);
//                    dataSourceMap.put(metaDbname.getName() + "_"+ WRITE, druidDataSourceWrite);
                    if (druidDataSourceRead !=null) {
                        dataSourceMap.put(metaDbname.getName() + "_"+ READ, druidDataSourceRead);
                    }
                    if (druidDataSourceWrite !=null) {
                        dataSourceMap.put(metaDbname.getName() + "_"+ WRITE, druidDataSourceWrite);
                    }

                });
            }
        } catch (Exception e){
            log.error("initDatasourceMap", e);
            throw e;
        }
    }


    private DruidDataSource generateOracleDatasource(MetaDbname metaDbname, String readOrWrite) {

        DruidDataSource druidDataSource = new DruidDataSource();

        if(StringUtils.equals(readOrWrite, WRITE)){

            //JDBC URL
            druidDataSource.setUrl(metaDbname.getDbW());
            //JDBC USERNAME
            druidDataSource.setUsername(metaDbname.getDbUserW());
            //JDBC PASSWORD
            druidDataSource.setPassword(metaDbname.getDbPasswordW());
            //JDBC NAME
            druidDataSource.setName(metaDbname.getName() + "-" + WRITE);
            //连接池允许的最大连接数
            druidDataSource.setMaxActive(Integer.parseInt(metaDbname.getDbConnectionsMaxW()));
            //最小空闲连接
            druidDataSource.setMinIdle(Integer.parseInt(metaDbname.getDbConnectionsMinW()));
        } else {


            //JDBC URL
            druidDataSource.setUrl(metaDbname.getDbR());
            //JDBC USERNAME
            druidDataSource.setUsername(metaDbname.getDbUserR());
            //JDBC PASSWORD
            druidDataSource.setPassword(metaDbname.getDbPasswordR());
            //JDBC NAME
            druidDataSource.setName(metaDbname.getName() + "-" + READ);
            //连接池允许的最大连接数
            druidDataSource.setMaxActive(Integer.parseInt(metaDbname.getDbConnectionsMaxR()));
            //最小空闲连接
            druidDataSource.setMinIdle(Integer.parseInt(metaDbname.getDbConnectionsMinR()));
            //对于只读连接，将连接池设置为Readonly模式。
            druidDataSource.setDefaultReadOnly(true);
        }

        this.setOracleCommonParameter(druidDataSource);

        //注入监控Filter对象
        List<Filter> fList = new ArrayList<>();
        //1.慢SQL记录Filter
        fList.add(new SlowSqlFilter());
        //2.SQL日志记录Filter
        fList.add(new SqlLogFilter());
        druidDataSource.setProxyFilters(fList);

        return druidDataSource;
    }

    /**
     * Oracle通用优化参数设置
     * @param druidDataSource
     */
    private void setOracleCommonParameter(DruidDataSource druidDataSource) {
        //********************************************************************
        //**数据源连接池通用化设置(目前仅针对Oracle优化，有Oracle独有优化参数。支持其他数据库时请调整)
        //连接池初始化连接数量
        druidDataSource.setInitialSize(2);
        //连接超时时间,10s
        druidDataSource.setMaxWait(1000 * 10);
        //多久才进行一次检测，检测需要关闭的空闲连接，2s
        druidDataSource.setTimeBetweenEvictionRunsMillis(1000 * 2);
        //一个连接在池中最小生存的时间,5分钟
        druidDataSource.setMinEvictableIdleTimeMillis(1000 * 60 * 5);
        //一个连接在池中最大生存的时间,15分钟
        druidDataSource.setMinEvictableIdleTimeMillis(1000 * 60 * 15);
        //测试SQL
        druidDataSource.setValidationQuery(SqlUtil.ORACLE_TEST_SQL);
        //空闲的时候进行连接测试
        druidDataSource.setTestWhileIdle(true);
        //申请连接时执行validationQuery检测连接是否有效
        druidDataSource.setTestOnBorrow(false);
        //归还连接时执行validationQuery检测连接是否有效
        druidDataSource.setTestOnReturn(false);
        //连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作，强制保留minIdle数量
        druidDataSource.setKeepAlive(false);
        //是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle
        druidDataSource.setPoolPreparedStatements(true);
        //preparedStatement缓存数量
        druidDataSource.setMaxOpenPreparedStatements(50);
        //周期性的将SQL统计信息打印到日志中，周期为5分钟
        druidDataSource.setTimeBetweenLogStatsMillis(1000 * 60 * 5);
    }

    /**
     * 获取元数据对应的数据库连接
     *
     * @param resclassenname 模型英文名
     * @param flag           READ/WRITE
     * @return JdbcTempalte连接
     * @throws NoPredinedJdbcTemplateFoundException 如果没有此数据库连接则抛出异常
     */
//    public JdbcTemplate getTemplateByResclass(String resclassenname, String flag) throws NoPredinedJdbcTemplateFoundException {
//
//        ResClassObj resClassObj = MetaInfo.getResClassObj(resclassenname);
//
//        if (resClassObj == null) {
//            throw new NoPredinedJdbcTemplateFoundException("模型英文名" + resclassenname + "在Redis中找不到，因此无法获取数据库连接！");
//        }
//
//        if (resClassObj.getResClassEx() == null) {
//            throw new NoPredinedJdbcTemplateFoundException("模型英文名" + resclassenname + "配置出错，没有ResClassEx");
//        }
//
//        String m3DbName = resClassObj.getResClassEx().getM3DbName();
//        if (StringUtils.isEmpty(m3DbName)) {
//            throw new NoPredinedJdbcTemplateFoundException("模型英文名" + resclassenname + "配置出错，没有Dbname");
//        }
//
//        JdbcTemplate jdbcTemplate = this.jdbcTemplateMap.get(m3DbName + "_" + flag);
//
//        if (jdbcTemplate == null) {
//            throw new NoPredinedJdbcTemplateFoundException("模型英文名" + resclassenname + "没有获取到JdbcTemplate");
//        }
//
//        return jdbcTemplate;
//    }
//
//    /**
//     * 根据数据库名称，获取对应的数据库连接
//     *
//     * @param m3DbName
//     * @param flag
//     * @return
//     * @throws NoPredinedJdbcTemplateFoundException
//     */
//    public JdbcTemplate getTemplateByDbName(String m3DbName, String flag) throws NoPredinedJdbcTemplateFoundException {
//        JdbcTemplate jdbcTemplate = this.jdbcTemplateMap.get(m3DbName + "_" + flag);
//        if (jdbcTemplate == null) {
//            throw new NoPredinedJdbcTemplateFoundException("数据库名称" + m3DbName + "没有获取到JdbcTemplate");
//        }
//
//        return jdbcTemplate;
//    }

//    /**
//     * 根据数据库名称，获取对应事务管理器
//     *
//     * @param m3DbName
//     * @param flag
//     * @return
//     * @throws NoPredinedJdbcTemplateFoundException
//     */
//    public DataSourceTransactionManager getTransactionManagerByDbName(String m3DbName, String flag) throws NoPredinedJdbcTemplateFoundException {
//        DataSourceTransactionManager transactionManager = this.transactionManagerMap.get(m3DbName + "_" + flag);
//        if (transactionManager == null) {
//            throw new NoPredinedJdbcTemplateFoundException("数据库名称" + m3DbName + "没有获取到transactionManager");
//        }
//
//        return transactionManager;
//    }

}
