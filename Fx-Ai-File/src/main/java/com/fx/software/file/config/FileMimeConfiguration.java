package com.fx.software.file.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;

/**
 * @FileName FileMimeConfiguration
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
public class FileMimeConfiguration {

    static final String CONFIG_FILE_ENCODING = "UTF-8";

    static final String CONFIG_COMMON_KEY = "common";

    Map<String, String> fileMimeMap;

    @PostConstruct
    void initConfigData() throws Exception {

        InputStream inputStream = null;
        try {

            ClassPathResource resource = new ClassPathResource("fileMimeConfig.cfg");
            inputStream = resource.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            fileMimeMap = mapper.readValue(inputStream, new TypeReference<Map<String, String>>() {
            });
            log.info("mime类型映射：{}", fileMimeMap);

        } catch (Exception e) {
            log.error("未找到fileMimeConfig.cfg配置文件");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

    public Map<String, String> getFileMimeMap() {
        return fileMimeMap;
    }

}
