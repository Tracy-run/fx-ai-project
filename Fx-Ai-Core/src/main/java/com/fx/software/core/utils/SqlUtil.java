package com.fx.software.core.utils;

import com.fx.software.core.security.SecurityUtils;
import com.fx.software.core.web.ResAttributeObj;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @FileName SqlUtil
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class SqlUtil {

    public static final String DATABASE_ORACLE = "oracle";
    public static final String INT_ID = "int_id";
    public static final String STATEFLAG = "stateflag";
    public static final String CREATOR = "creator";
    public static final String CREAT_TIME = "creat_time";
    public static final String MODIFIER = "modifier";
    public static final String MODIFY_TIME = "modify_time";
    public static final String TIME_STAMP = "time_stamp";
    public static final String SQL_OPERATION_INSERT = "insert";
    public static final String SQL_OPERATION_UPDATE = "update";
    public static final String SQL_OPERATION_DELETE = "delete";
    public static final String DATATYPE_DATETODAY = "datetoday";
    public static final String DATATYPE_DATETOSECOND = "datetosecond";
    public   SnowFlakeIdGenerator snowFlakeIdGenerator;
    public static final String ORACLE_JDBC_PREFIX = "jdbc:oracle:thin:@";
    public static final String ORACLE_TEST_SQL = "select 1 from dual";


    @Autowired
    public SqlUtil(SnowFlakeIdGenerator snowFlakeIdGenerator) {
        this.snowFlakeIdGenerator = snowFlakeIdGenerator;
    }

//    public static String generateOracleInsertSql(String className, Set<String> attrNameSet) {
//
//        ResClassObj resClassObj = MetaInfo.getResClassObj(className);
//
//        String tableName = resClassObj.getDsTableName();
//        StringBuilder sql = new StringBuilder();
//        sql.append("insert into ").append(tableName).append("(");
//
//        //获取模型属性信息
//        Map<String, ResAttributeObj> allAttributeMap = resClassObj.getAttributeObjMap();
//
//        int num = 0;
//        for (Object attrName : attrNameSet) {
//            String columnName = allAttributeMap.get(attrName).getAttributeColumnName();
//            sql.append(columnName);
//            num++;
//            if (num < attrNameSet.size()) {
//                sql.append(",");
//            }
//        }
//        sql.append(")");
//        sql.append(" values(");
//
//        num = 0;
//        for (Object attrName : attrNameSet) {
//
//            String columnName = allAttributeMap.get(attrName).getAttributeColumnName();
//            sql.append("?");
//
//            num++;
//            if (num < attrNameSet.size()) {
//                sql.append(",");
//            }
//        }
//        ;
//        sql.append(")");
//        String sqlStr = sql.toString();
//        log.info(sqlStr);
//        return sqlStr;
//    }

    /**
     * 生成oracle更新语句
     *
     * @param className   模型英文名称
     * @param attrNameSet 属性set
     * @return sql语句
     */
//    public static String generateOracleUpdateSql(String className, Set<String> attrNameSet, Set<String> orderedAttrSet) {
//
//        ResClassObj resClassObj = MetaInfo.getResClassObj(className);
//
//        String tableName = resClassObj.getDsTableName();
//        StringBuilder sql = new StringBuilder();
//        sql.append("update ").append(tableName).append(" set ");
//
//        StringBuilder whereSql = new StringBuilder(" where ");
//        //获取模型属性信息
//        Map<String, ResAttributeObj> allAttributeMap = resClassObj.getAttributeObjMap();
//
//        String intIdAttrName = "";
//        for (Object attrName : attrNameSet) {
//            String columnName = allAttributeMap.get(attrName).getAttributeColumnName();
//
//            if (!INT_ID.equalsIgnoreCase(columnName)) {
//                orderedAttrSet.add((String) attrName);
//                sql.append(columnName).append("=?,");
//            } else {
//                intIdAttrName = (String) attrName;
//                whereSql.append(columnName).append("=?");
//                ;
//            }
//        }
//
//        orderedAttrSet.add(intIdAttrName);
//
//        String str = "?,";
//        if (sql.toString().endsWith(str)) {
//            sql.deleteCharAt(sql.length() - 1);
//        }
//        sql.append(whereSql);
//        String sqlStr = sql.toString();
//        log.info(sqlStr);
//        return sqlStr;
//    }
//
//    /**
//     * 生成oracle删除语句
//     *
//     * @param className 模型英文名称
//     * @return sql语句
//     */
//    public static String generateOracleDeleteSql(String className) {
//
//        ResClassObj resClassObj = MetaInfo.getResClassObj(className);
//
//        String tableName = resClassObj.getDsTableName();
//        StringBuilder sql = new StringBuilder();
//        sql.append("update ").append(tableName).append(" set stateflag=?  where int_id=? ");
//        String sqlStr = sql.toString();
//        log.info(sqlStr);
//        return sqlStr;
//    }

    public static String getStateflag() {
        SimpleDateFormat sf = new SimpleDateFormat("yyMMddHHmm");

        return sf.format(new Date());
    }

    /**
     * 根据格式获取时间字符串
     *
     * @param pattern
     * @return
     */
    public static String getDateString(String pattern) {
        SimpleDateFormat sf = new SimpleDateFormat(pattern);
        return sf.format(new Date());
    }

    public static Date parseDate(String dateStr, String pattern) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat(pattern);
            return sf.parse(dateStr);
        } catch (ParseException e) {
            log.error("parseDate：", e);
            return null;
        }
    }


    private  void setInsertAttribute(Map<String, ResAttributeObj> resAttributeObjMap, Map<String, String> valueMap) {
        if (resAttributeObjMap.containsKey(INT_ID)) {
            if (!valueMap.containsKey(INT_ID) || TextUtil.isNull(valueMap.get(INT_ID))) {
                valueMap.put("int_id", String.valueOf(snowFlakeIdGenerator.nextId()));
            }
        }

        if (resAttributeObjMap.containsKey(CREATOR)) {

            if (!valueMap.containsKey(CREATOR) || TextUtil.isNull(valueMap.get(CREATOR))) {
                Optional<String> optional = SecurityUtils.getCurrentUserLogin();
                String userName = optional.get();
                valueMap.put("creator", userName);
            }

        }
        if (resAttributeObjMap.containsKey(CREAT_TIME)) {

            if (!valueMap.containsKey(CREAT_TIME) || TextUtil.isNull(valueMap.get(CREAT_TIME))) {
                ResAttributeObj resAttributeObj = resAttributeObjMap.get("creat_time");

                if (DATATYPE_DATETODAY.equalsIgnoreCase(resAttributeObj.getDataType())) {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd");
                    valueMap.put("creat_time", dateStr);
                } else if (DATATYPE_DATETOSECOND.equalsIgnoreCase(resAttributeObj.getDataType())) {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd HH:mm:ss");
                    valueMap.put("creat_time", dateStr);
                } else {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd HH:mm:ss");
                    valueMap.put("creat_time", dateStr);
                }
            }


        }
    }

    private static void setUpdateAttribute(Map<String, ResAttributeObj> resAttributeObjMap, Map<String, String> valueMap) {
        if (resAttributeObjMap.containsKey(MODIFIER)) {

            if (!valueMap.containsKey(MODIFIER) || TextUtil.isNull(valueMap.get(MODIFIER))) {
                Optional<String> optional = SecurityUtils.getCurrentUserLogin();
                if(optional!=null){
                    String userName = optional.get();
                    valueMap.put("modifier", userName);
                }

            }
        }
        if (resAttributeObjMap.containsKey(MODIFY_TIME)) {

            if (!valueMap.containsKey(MODIFY_TIME) || TextUtil.isNull(valueMap.get(MODIFY_TIME))) {
                ResAttributeObj resAttributeObj = resAttributeObjMap.get("modify_time");

                if (DATATYPE_DATETODAY.equalsIgnoreCase(resAttributeObj.getDataType())) {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd");
                    valueMap.put("modify_time", dateStr);
                } else if (DATATYPE_DATETOSECOND.equalsIgnoreCase(resAttributeObj.getDataType())) {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd HH:mm:ss");
                    valueMap.put("modify_time", dateStr);
                } else {
                    String dateStr = SqlUtil.getDateString("yyyy-MM-dd HH:mm:ss");
                    valueMap.put("modify_time", dateStr);
                }
            }
        }
    }


}
