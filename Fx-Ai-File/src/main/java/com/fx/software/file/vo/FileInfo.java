package com.fx.software.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @FileName FileInfo
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo implements Serializable {

    private static final long serialVersionUID = -9160911789904102920L;


    public static final int STATUS_ERROR = 1;
    public static final int STATUS_SUCCESS = 0;

    /**
     * 主键
     */
    private String id;
    /**
     * 附件名称
     */
    private String fileName;
    /**
     * 附件类型
     */
    private String fileType;
    /**
     * 附件大小
     */
    private String fileSize;
    /**
     * 附件路径
     */
    private String filePath;
    /**
     * 上传时间
     */
    private Date createTime;
    /**
     * 上传人ID
     */
    private String creatorId;
    /**
     * 上传人名称
     */
    private String creatorName;
    /**
     * 上传人IP
     */
    private String creatorIp;
    /**
     * 下载次数
     */
    private Integer downloadCount;
    /**
     * 工单流水号
     */
    private String flowId;
    /**
     * 工单编号
     */
    private String flowNo;
    /**
     * 工单主题
     */
    private String flowTitle;
    /**
     * 工单类型
     */
    private String flowType;
    /**
     * 工单类型标识
     */
    private String flowTypeid;
    /**
     * 流程实例ID
     */
    private String processinstid;
    /**
     * 流程定义英文名
     */
    private String processdefname;
    /**
     * 流程定义中文名
     */
    private String processchname;
    /**
     * 流程定义版本
     */
    private String version;
    /**
     * 任务标识
     */
    private String taskid;
    /**
     * 环节英文名
     */
    private String taskdefname;
    /**
     * 环节中文名
     */
    private String taskchname;
    /**
     * 省份名称
     */
    private String provinceName;


    private String saveType;
    /**
     * 附件描述
     */
    private String fileDesc;
    /**
     * 环节的场景名称，用于前台组件在同一环节分类
     */
    private String taskscence;

    private int pageNo;

    private int pageSize;

    private String queryFilter;

}
