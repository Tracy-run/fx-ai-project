package com.fx.software.core.config;

import com.fx.software.core.async.ExceptionHandlingAsyncTaskExecutor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @FileName AsyncConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Autowired
    private TinyConfiguration configuration;

    public AsyncConfiguration() {
    }

    @Bean( name = {"taskExecutor"})
    @Override
    public Executor getAsyncExecutor() {
        this.log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.configuration.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(this.configuration.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(this.configuration.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("demo-default-executor-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(this.scheduledTaskExecutor());
    }
    @Bean
    public Executor scheduledTaskExecutor() {
        return new ScheduledThreadPoolExecutor(5, (new BasicThreadFactory.Builder()).namingPattern("schedule-pool-%d").daemon(true).build());
    }
}
