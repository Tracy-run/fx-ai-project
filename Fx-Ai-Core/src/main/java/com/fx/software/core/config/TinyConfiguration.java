package com.fx.software.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.hibernate.validator.constraints.Length;
/**
 * @FileName TinyConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Component
@ConfigurationProperties(
        prefix = "tiny",
        ignoreUnknownFields = true,
        ignoreInvalidFields = false
)
@PropertySources({@PropertySource(
        value = {"classpath:git.properties"},
        ignoreResourceNotFound = true
), @PropertySource(
        value = {"classpath:META-INF/build-info.properties"},
        ignoreResourceNotFound = true
)})
public class TinyConfiguration {

    private final com.fx.software.core.config.TinyConfiguration.Security security = new com.fx.software.core.config.TinyConfiguration.Security();
    private final com.fx.software.core.config.TinyConfiguration.Async async = new com.fx.software.core.config.TinyConfiguration.Async();
    private final com.fx.software.core.config.TinyConfiguration.ApiDoc apidoc = new com.fx.software.core.config.TinyConfiguration.ApiDoc();
    private final com.fx.software.core.config.TinyConfiguration.Cache cache = new com.fx.software.core.config.TinyConfiguration.Cache();
    private final com.fx.software.core.config.TinyConfiguration.SnowFlake snowFlake = new com.fx.software.core.config.TinyConfiguration.SnowFlake();
    private final com.fx.software.core.config.TinyConfiguration.ControllerException controllerException = new com.fx.software.core.config.TinyConfiguration.ControllerException();

    public TinyConfiguration() {
    }

    public com.fx.software.core.config.TinyConfiguration.Security getSecurity() {
        return this.security;
    }

    public com.fx.software.core.config.TinyConfiguration.Async getAsync() {
        return this.async;
    }

    public com.fx.software.core.config.TinyConfiguration.ApiDoc getApidoc() {
        return this.apidoc;
    }

    public com.fx.software.core.config.TinyConfiguration.Cache getCache() {
        return this.cache;
    }

    public com.fx.software.core.config.TinyConfiguration.SnowFlake getSnowFlake() {
        return this.snowFlake;
    }


    public class Async {
        private int corePoolSize = 5;
        private int maxPoolSize = 50;
        private int queueCapacity = 10000;

        public Async() {
        }

        public int getCorePoolSize() {
            return this.corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return this.maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return this.queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public class ApiDoc {
        private String apiBasePackage = "com.fx";

        public ApiDoc() {
        }

        public String getApiBasePackage() {
            return this.apiBasePackage;
        }

        public void setApiBasePackage(String apiBasePackage) {
            this.apiBasePackage = apiBasePackage;
        }
    }

    public class SnowFlake {
        public static final String STRATEGY_MANUAL = "manual";
        public static final String STRATEGY_RANDOM = "random";
        public static final String STRATEGY_IPV4 = "ipv4";
        private int dataCenterId;
        private int machineId;
        private String workerIdAutoGenerateStrategy = "random";

        public SnowFlake() {
        }

        public int getDataCenterId() {
            return this.dataCenterId;
        }

        public void setDataCenterId(int dataCenterId) {
            this.dataCenterId = dataCenterId;
        }

        public int getMachineId() {
            return this.machineId;
        }

        public void setMachineId(int machineId) {
            this.machineId = machineId;
        }

        public String getWorkerIdAutoGenerateStrategy() {
            return this.workerIdAutoGenerateStrategy;
        }

        public void setWorkerIdAutoGenerateStrategy(String workerIdAutoGenerateStrategy) {
            this.workerIdAutoGenerateStrategy = workerIdAutoGenerateStrategy;
        }
    }

    public class Security {
        private final com.fx.software.core.config.TinyConfiguration.Security.Jwt jwt = new com.fx.software.core.config.TinyConfiguration.Security.Jwt();
        private String exclude;
        private boolean authContextAutoInject = false;

        public Security() {
        }

        public String getExclude() {
            return this.exclude;
        }

        public void setExclude(String exclude) {
            this.exclude = exclude;
        }

        public boolean isAuthContextAutoInject() {
            return this.authContextAutoInject;
        }

        public void setAuthContextAutoInject(boolean authContextAutoInject) {
            this.authContextAutoInject = authContextAutoInject;
        }

        public com.fx.software.core.config.TinyConfiguration.Security.Jwt getJwt() {
            return this.jwt;
        }

        public class Jwt {
            @Length(min = 64)
            private String secret = "3XKfzFReDSSipqnmYYbXNt6X9GBq83zzuW8N77sOtlGr8aLp0IxbYABRgU7HSNSr";

            public Jwt() {
            }

            public String getSecret() {
                return this.secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }
        }
    }

    public class Cache {
        private int redisDefaultTTL = 1800;

        public Cache() {
        }

        public int getRedisDefaultTTL() {
            return this.redisDefaultTTL;
        }

        public void setRedisDefaultTTL(int redisDefaultTTL) {
            this.redisDefaultTTL = redisDefaultTTL;
        }
    }

    public class ControllerException {
        private boolean enabled = false;

        public ControllerException() {
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }




}
