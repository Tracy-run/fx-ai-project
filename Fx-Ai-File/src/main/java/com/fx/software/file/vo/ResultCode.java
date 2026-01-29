package com.fx.software.file.vo;

/**
 * @FileName ResultCode
 * @Description
 * @Author fx
 * @date 2026-01-29
 */
public enum  ResultCode {

    /**
     * 成功
     */
    SUCCESS(0),
    /**
     * 失败
     */
    FAILURE(1),

    /**
     * 批量部分失败
     */
    RAW_FAILURE(2),
    /**
     * 业务错误
     */
    BUSINESS_FAIL(101),
    /**
     * 未知错误
     */
    UNKOWN_FAIL(201);

    private int val;

    private ResultCode(int value) {
        this.val = value;
    }

    public int value() {
        return this.val;
    }
}
