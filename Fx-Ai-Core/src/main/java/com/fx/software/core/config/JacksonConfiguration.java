package com.fx.software.core.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

/**
 * @FileName JacksonConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class JacksonConfiguration {

        public JacksonConfiguration() {
            // 无参构造方法，原始代码中可能为空（反编译后保留）
        }

        // 注册Jackson的Java 8时间模块（处理LocalDateTime等时间类型序列化）
        @Bean
        public JavaTimeModule javaTimeModule() {
            return new JavaTimeModule();
        }

        // 注册Jackson的JDK8模块（处理Optional等JDK8特性）
        @Bean
        public Jdk8Module jdk8TimeModule() {
            return new Jdk8Module();
        }

        // 注册Afterburner模块（提升Jackson序列化/反序列化性能）
        @Bean
        public AfterburnerModule afterburnerModule() {
            return new AfterburnerModule();
        }

        // 注册Problem模块（处理HTTP异常的标准化响应）
        @Bean
        public ProblemModule problemModule() {
            return new ProblemModule();
        }

        // 注册ConstraintViolationProblem模块（处理参数校验异常的标准化响应）
        @Bean
        public ConstraintViolationProblemModule constraintViolationProblemModule() {
            return new ConstraintViolationProblemModule();
        }
}
