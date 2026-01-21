package com.fx.software.core.config;

import com.fx.software.core.security.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.util.Map;

/**
 * @FileName KafkaProducerSecurityInterceptor
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class KafkaProducerSecurityInterceptor implements ProducerInterceptor<String, Object> {


    public KafkaProducerSecurityInterceptor() {
    }

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> producerRecord) {
        String jwt = null;
        String useraccount = null;
        String requestId = null;
        String txContext = null;
        if (SecurityUtils.getCurrentUserJWT().isPresent()) {
            jwt = (String)SecurityUtils.getCurrentUserJWT().get();
        }

        if (SecurityUtils.getCurrentUserLogin().isPresent()) {
            useraccount = (String)SecurityUtils.getCurrentUserLogin().get();
        }

        if (SecurityUtils.getRequestId().isPresent()) {
            requestId = (String)SecurityUtils.getRequestId().get();
        }

        if (SecurityUtils.getTxContext().isPresent()) {
            txContext = (String)SecurityUtils.getTxContext().get();
        }

        // 1. 添加jwt到Header
        if (StringUtils.isNotEmpty(jwt)) {
            // 原始代码中直接用匿名内部类或RecordHeader（Kafka自带Header实现）
            Header hJwt = new RecordHeader("jwt", jwt.getBytes()); // 实际Kafka中常用RecordHeader
            producerRecord.headers().add(hJwt);
        }

        // 2. 添加useraccount到Header
        if (StringUtils.isNotEmpty(useraccount)) {
            Header hUseraccount = new RecordHeader("useraccount", useraccount.getBytes());
            producerRecord.headers().add(hUseraccount);
        }

        // 3. 添加requestId到Header
        if (StringUtils.isNotEmpty(requestId)) {
            Header hRequestId = new RecordHeader("requestId", requestId.getBytes());
            producerRecord.headers().add(hRequestId);
        }

        // 4. 添加txContext到Header
        if (StringUtils.isNotEmpty(txContext)) {
            Header hTxContext = new RecordHeader("txContext", txContext.getBytes());
            producerRecord.headers().add(hTxContext);
        }

        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        // 原始代码中此处是回调逻辑（比如处理发送成功/失败）
        if (e != null) {
            e.printStackTrace();
        } else {
            // 处理成功逻辑
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
