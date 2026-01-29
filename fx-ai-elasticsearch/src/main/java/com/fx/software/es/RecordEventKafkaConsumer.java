package com.fx.software.es;

import com.fx.software.core.utils.KafkaUtil;
import com.fx.software.es.config.JdbcDatasource;
import com.fx.software.es.service.RedisService;
import com.fx.software.es.util.WorkerThread;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @FileName RecordEventKafkaConsumer
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Component
@Slf4j
@Order(value = 8)
public class RecordEventKafkaConsumer  implements CommandLineRunner {

    @Value("${synThreadNumber}")
    private int numThreads;
    String topicName= KafkaUtil.KAFKA_TOPIC_GLOBAL_SEARCH;
    @Value("${group_id}")
    String groupid;
    @Value("${bootstrap_servers}")
    String bootstrapservers;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RedisService redisService;
    @Value("${syn_config_file_path}")
    String synconfigfilepath;
    @Autowired
    JdbcDatasource myDatasource;
    @Override
    public void run(String... args) throws Exception {
        ExecutorService threadPool = newFixedThreadPool(numThreads);
        WorkerThread workerThread=   new WorkerThread(myDatasource, synconfigfilepath, restHighLevelClient,  redisService,  topicName,  groupid,  bootstrapservers );
        threadPool.execute(workerThread);
        // 让线程池执行任务 task
        threadPool.shutdown();
    }
}
