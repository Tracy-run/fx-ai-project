package com.fx.software.file.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @FileName ImageInitData
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Data
public class ImageInitData implements Serializable {

    /**
     * 图片http地址，多个地址用逗号分隔，必填
     */
    private String picUrl;
    /**
     * 图片请求方法 GET POST
     */
    private String picRequestMethod;
    /**
     * 请求属性 json格式 {"key1":"value","key2":"value2"}
     */
    private String picRequestProperty;
    /**
     * 模型英文名称，必填
     */
    private String modelEnname;
    /**
     * 资源ID，必填
     */
    private String resId;
    /**
     * 资源名称
     */
    private String resName;
    /**
     * 图片来源
     */
    private String picFrom;
    /**
     * 流程ID
     */
    private String flowId;
    /**
     * 环节ID
     */
    private String workItemId;
    /**
     * 扩展属性1
     */
    private String attr1;
    /**
     * 扩展属性2
     */
    private String attr2;
    /**
     * 上传用户
     */
    private String uploadUser;
}
