package com.fx.software.es;

import com.fx.software.core.utils.KafkaUtil;
import com.fx.software.es.util.DecodeingKafka;
import com.fx.software.es.util.RelationCache;
import com.fx.software.es.util.TransResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @FileName TransResultKafkaConsumer
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Component
@Slf4j
@Order(value = 6)
public class TransResultKafkaConsumer extends Thread implements CommandLineRunner {

    String topicName=  KafkaUtil.KAFKA_TOPIC_TRANS_NAME;
    @Value("${group_id}")
    String groupid;
    @Value("${bootstrap_servers}")
    String bootstrapservers;
    @Value("${synThreadNumber}")
    private int numThreads;

    @Autowired
    private RedisTemplate<String, Object> strRedisTemplate;


    @Override
    public void run() {
        KafkaConsumer<String, Object> consumer = null;
        Properties props = new Properties();
        ExecutorService threadPool = newFixedThreadPool(numThreads);
        props.put("bootstrap.servers", bootstrapservers);
        props.put("group.id", groupid);
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("max.poll.records", "100");
        props.put("enable.auto.commit", "true");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", DecodeingKafka.class.getName());
        ConsumerConfig config = new ConsumerConfig(props);
        try {
            consumer = new KafkaConsumer<>(props);
        } catch (Exception e) {
            log.error("kafka建立{}连接异常 {}", bootstrapservers, e.getMessage());

        }
        consumer.subscribe(Arrays.asList(topicName));
        long offset = 1L;

        int count = 0;

        while (true) {
            try {
                //每间隔500毫秒去kafka服务拉取一次数据String, Object

                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                //每次拉取的消息条数
                count = records.count();

                for (ConsumerRecord<String, Object> record : records) {
                    //将数据强转成sqldata对象
                    TransResultEvent transResultEvent = null;
                    try {
                        transResultEvent = (TransResultEvent) record.value();
                    } catch (Exception e) {
                        log.error("Id2NameResultEvent对象转换错误，传入的对象为非Id2NameResultEvent {}", record);
                        offset = record.offset();
                        continue ;
                    }

                    //获取偏移量


                    if (saveName2IdResultEventToRedis(   transResultEvent)) {
                        offset = record.offset();
                    }

                    //这里就可以对接收到的数据进行相关业务处理了
                }
                if (count > 0 && consumer != null) {
                    consumer.commitSync();
                }

            } catch (Exception e) {
                log.error("kafka拉取 " + topicName + "数据异常 {}", e.getMessage());

            }
        }
    }


    @Override
    public void run(String... args) throws Exception {
        if (numThreads < 0) {
            numThreads = 2;
        }
        ExecutorService threadPool = newFixedThreadPool(numThreads);
        threadPool.execute(this);
        // 让线程池执行任务 task
    }


    /**
     * 将Name2Id对应的关系信息存储到redis
     * @param transResultEvent 将名称与id的关系存储到redis
     */
    public boolean saveName2IdResultEventToRedis(TransResultEvent  transResultEvent) {

        try {
            if(transResultEvent!=null) {
                String classEnName = transResultEvent.getClassEnName();
                if (classEnName != null) {
                    classEnName = classEnName.toUpperCase();
                    String key = "id2name:" + classEnName + ":" + transResultEvent.getIntId();
                    String val = transResultEvent.getZhLabel();
                    Map<String, String> map = RelationCache.relstable.get(classEnName);
                    if (map == null) {
                        map = new HashMap<>(1);
                    }
                    //缓存翻译表数据信息
                    String opFlag = transResultEvent.getOpFlag();
                    if (opFlag == null) {
                        opFlag = "add";
                    }
                    opFlag = opFlag.toLowerCase();
                    if ("add".equals(opFlag) || "update".equals(opFlag)) {
                        map.put(transResultEvent.getIntId(), transResultEvent.getZhLabel());
                        RelationCache.relstable.put(classEnName,map);
                        String rkey = "name2id:" + classEnName + ":" + transResultEvent.getIndexValue();
                        String rval = transResultEvent.getIntId();
                        try {
                            strRedisTemplate.opsForValue().set(key, val);
                            strRedisTemplate.opsForValue().set(rkey, rval);
                        } catch (Exception e) {
                            log.error("数据插入缓存异常"+e.getMessage());
                        }
                    } else {
                        map.remove(transResultEvent.getIntId());
                        RelationCache.relstable.put(classEnName,map);
                        String rkey = "name2id:" + classEnName + ":" + transResultEvent.getIndexValue();
                        RelationCache.relstable.get(classEnName).remove(transResultEvent.getIndexValue());
                        try {
                            strRedisTemplate.delete(rkey);
                            strRedisTemplate.delete(key);
                        } catch (Exception e) {
                            log.error("缓存数据删除异常"+e.getMessage());
                        }
                    }
                } else {
                    log.warn("原数据模型英文名为空");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("saveName2IdResultEventToRedis:", e);
            return false;
        }

        return true;

    }

}
