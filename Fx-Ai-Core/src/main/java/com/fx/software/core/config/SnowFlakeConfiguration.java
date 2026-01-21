package com.fx.software.core.config;

import com.fx.software.core.exception.SnowFlakeGenInitException;
import com.fx.software.core.utils.SnowFlakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * @FileName SnowFlakeConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
@Configuration
public class SnowFlakeConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SnowFlakeConfiguration.class);
    @Autowired
    TinyConfiguration tinyConfiguration;

    private final String LOCAL = "127.0.0.1";
    private final int IPV4_SEGMENT = 4;
    private final String SNOWFLAKE_GEN_EXCEPTION_MSG = "SnowFlakeIdGenerator初始化错误";

    public SnowFlakeConfiguration() {
    }

    @Bean
    SnowFlakeIdGenerator getSnowFlakeIdGenerator() throws SnowFlakeGenInitException {
        String strategy = this.tinyConfiguration.getSnowFlake().getWorkerIdAutoGenerateStrategy();
        int dataCenterId;
        int dataMachineId;
        if ("manual".equals(strategy)) {
            dataCenterId = this.tinyConfiguration.getSnowFlake().getDataCenterId();
            dataMachineId = this.tinyConfiguration.getSnowFlake().getMachineId();
            log.info("雪花算法生成！生成策略为：{}，数据中心ID为：{}，机器ID为：{}", new Object[]{strategy, dataCenterId, dataMachineId});
            return new SnowFlakeIdGenerator((long)dataCenterId, (long)dataCenterId);
        } else {
            if ("random".equals(strategy)) {
                Random random = new Random();
                dataCenterId = random.nextInt(4);
                dataMachineId = random.nextInt(256);
                log.info("雪花算法生成！生成策略为：{}，数据中心ID为：{}，机器ID为：{}", new Object[]{strategy, dataCenterId, dataMachineId});
                return new SnowFlakeIdGenerator((long)dataCenterId, (long)dataCenterId);
            } else if ("ipv4".equals(strategy)) {
                String hostAddress = "127.0.0.1";

                try {
                    hostAddress = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException var6) {
                }

                String[] ipGroup = StringUtils.split(hostAddress, ",");
                if (ipGroup == null) {
                    log.error("SNOWFLAKE配置错误，请检查配置和系统环境。当前的策略为：{}", strategy);
                    throw new SnowFlakeGenInitException("SnowFlakeIdGenerator初始化错误");
                } else if (ipGroup.length == 4) {
                    dataCenterId = Integer.valueOf(ipGroup[2]) % 4;
                    int machineId = Integer.valueOf(ipGroup[3]) % 256;
                    log.info("雪花算法生成！生成策略为：{}，数据中心ID为：{}，机器ID为：{}", new Object[]{strategy, dataCenterId, machineId});
                    return new SnowFlakeIdGenerator((long)dataCenterId, (long)machineId);
                } else {
                    log.error("SNOWFLAKE配置错误，请检查配置和系统环境。当前的策略为：{}", strategy);
                    throw new SnowFlakeGenInitException("SnowFlakeIdGenerator初始化错误");
                }
            } else {
                log.error("SNOWFLAKE配置错误，请检查配置和系统环境。当前的策略为：{}", strategy);
                throw new SnowFlakeGenInitException("SnowFlakeIdGenerator初始化错误");
            }
        }
    }
}
