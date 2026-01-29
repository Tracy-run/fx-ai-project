package com.fx.software.core.utils;

/**
 * @FileName KafkaUtil
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
public class KafkaUtil {

    /**
     * 导出topic
     */
    public static final String KAFKA_TOPIC_EXPORT = "exp_by_schema";

    /**
     * 导出结束通知topic
     */
    public static final String KAFKA_TOPIC_EXPORT_COMPLETE = "exp_by_schema_complete";

    /**
     * 导入topic
     */
    public static final String KAFKA_TOPIC_IMPORT = "imp_by_schema";

    /**
     * 导入topic
     */
    public static final String KAFKA_TOPIC_IMPORT_COMPLETE = "imp_by_schema_complete";

    /**
     * 多模型导入topic
     */
    public static final String KAFKA_TOPIC_MULTI_IMPORT = "multi_imp_by_schema";

    /**
     * 多模型导入结束topic
     */
    public static final String KAFKA_TOPIC_MULTI_IMPORT_COMPLETE = "multi_imp_by_schema_complete";

    /**
     * 全局搜索topic
     */
    public static final String KAFKA_TOPIC_GLOBAL_SEARCH = "recordevent";

    /**
     * 根据关联资源修改shape topic
     */
    public static final String KAFKA_TOPIC_RESOURCE_SHAPE = "resourceShape";

    /**
     * 正反向翻译topic
     */
    public static final String KAFKA_TOPIC_TRANS_NAME = "TransResult";

    public static final String GIS_RESOURCE_TOPIC = "gisResourceTopic";

    /**
     * 属性模型topic
     */
    public static final String KAFKA_TOPIC_REFRESHMETA = "refreshMeta";

    /**
     * 导出时一批次数据量
     */
    public static final int DATA_BATCHNUM_EXPORT = 500;

    public static final int DATA_BATCHNUM_IMPORT = 500;

    public static final String INSERT = "insert";

    public static final String UPDATE = "update";

    public static final String ONE_STR = "1";

    public static final String TWO_STR = "2";

    public static final String THREE_STR = "3";

    public static final String FOUR_STR = "4";

    public static final Integer TWO_INT = 2;

    public static final Integer THREE_INT = 3;
}
