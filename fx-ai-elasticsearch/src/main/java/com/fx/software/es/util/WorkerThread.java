package com.fx.software.es.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fx.software.es.config.JdbcDatasource;
import com.fx.software.es.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

/**
 * @FileName WorkerThread
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class WorkerThread extends Thread {

    BulkProcessor bulkProcessor;

    private RestHighLevelClient restHighLevelClient;
    private RedisService redisService;
    String topicName;
    String groupid;
    String bootstrapservers;
    String synconfigfilepath;
    JdbcDatasource myDatasource;

    public WorkerThread(JdbcDatasource myDatasource, String synconfigfilepath, RestHighLevelClient restHighLevelClient, RedisService redisService, String topicName, String groupid, String bootstrapservers) {
        this.bootstrapservers = bootstrapservers;
        this.groupid = groupid;
        this.topicName = topicName;
        this.restHighLevelClient = restHighLevelClient;
        this.redisService = redisService;
        this.synconfigfilepath = synconfigfilepath;
        this.myDatasource = myDatasource;
    }


    @Override
    public void run() {
        bulkProcessor = BulkProcessor.builder((request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(1L))
                .setConcurrentRequests(1)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
        configProperties();
        //建立kafka连接
        KafkaConsumer<String, Object> consumer = null;
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapservers);
        props.put("group.id", groupid);
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("max.poll.records", "50");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", DecodeingKafka.class.getName());
        props.put("enable.auto.commit", "true");
        ConsumerConfig config = new ConsumerConfig(props);
        try {
            consumer = new KafkaConsumer<>(props);
        } catch (Exception e) {
            log.error("kafka建立{}连接异常 {}", bootstrapservers, e.getMessage());
        }
        consumer.subscribe(Arrays.asList(topicName));
//        DataOp dataOp = null;
        int count = 0;
        ConsumerRecords<String, Object> records = null;
        while (true) {
            long offset = 1L;
            try {
                //一次拉去500条数据
                records = consumer.poll(Duration.ofMillis(500));
                //每次拉取的消息条数
                count = records.count();
                for (ConsumerRecord<String, Object> record : records) {
                    //将数据强转成sqldata对象
                    try {
//                        dataOp = (DataOp) record.value();
                    } catch (Exception e) {
                        log.error("Id2NameResultEvent对象转换错误，传入的对象为非Id2NameResultEvent {}", record);
                    }
                    Date currentTime = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
                    String dateString = formatter.format(currentTime);

//                    Map<String, List<Map>> insertData = dataOp.getInsertData();
//                    if (insertData != null && insertData.size() > 0) {
//                        insertData(insertData, dateString);
//                    }
//                    Map<String, List<Long>> deleteData = dataOp.getDeleteData();
//                    if (deleteData != null && deleteData.size() > 0) {
//                        deleteData(deleteData);
//                    }
//                    Map<String, List<Map>> updateData = dataOp.getUpdateData();
//                    if (updateData != null && updateData.size() > 0) {
//                        updateData(updateData, dateString);
//                    }
                    offset = record.offset();
                }
                if (count > 0 && consumer != null) {

                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("kafka拉取 " + topicName + "数据异常 {}", e.getMessage());
            }
        }
    }


    /**
     * 读取model.json配置信息
     * @return
     */
    public boolean configProperties() {
        try {
            String s = readJsonFile(synconfigfilepath);
            if (s != null) {
                JSONObject jobj = JSON.parseObject(s);
                JSONArray links = jobj.getJSONArray("cache_relmeta");
                //获取要缓存的数据
                if (links != null) {
                    for (int i = 0; i < links.size(); i++) {
                        String resClassEnName = "" + links.get(i);
                        resClassEnName = resClassEnName.toUpperCase();
                        //ResClassObj resClassObj = MetaInfo.getResClassObj(resClassEnName);
//                        if (resClassObj != null) {
//
//                            String dsTableName = resClassObj.getDsTableName();
//
//                            String sql = " select  int_id  , zh_label from  " + dsTableName;
//                            //得到数据库的动态链接
//                            JdbcTemplate jdbcTemplate1 = myDatasource.getTemplateByResclass(resClassEnName, JdbcDatasource.READ);
//                            loadRelationData(jdbcTemplate1, sql, resClassEnName);
//                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.error("configProperties方法执行错误" + e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * 读取json文件，返回json串
     *
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        FileReader fileReader = null;
        Reader reader = null;
        try {
            File jsonFile = new File(fileName);
            fileReader = new FileReader(jsonFile);
            reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }

            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            log.error("读取配置文件" + fileName + "错误，请参见src目录下面的model.json文件{}", e.getMessage());
            return null;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    log.error("fileReader.close()只从错误" + e.getMessage());
                }

            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("reader.close()只从错误" + e.getMessage());
                }
            }
        }
    }

    /**
     * 批量入es监听方法
     */
    private BulkProcessor.Listener listener = new BulkProcessor.Listener() {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            int numberOfActions = request.numberOfActions();
            log.debug("Executing bulk [{}] with {} requests",
                    executionId, numberOfActions);
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request,
                              BulkResponse response) {
            if (response.hasFailures()) {
                log.info("Bulk [{}] executed with failures", executionId);
            } else {
                log.info("Bulk [{}] completed in {} milliseconds",
                        executionId, response.getTook().getMillis());
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request,
                              Throwable failure) {
            log.error("Failed to execute bulk", failure);
        }
    };

}
