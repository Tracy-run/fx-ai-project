package com.fx.software.core.web;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @FileName ResAttributeObj
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Slf4j
@Data
public class ResAttributeObj implements Serializable {

    /**
     * 字段类型
     */
    private String dataType;
}
