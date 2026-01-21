package com.fx.software.core.web;

/**
 * @FileName ResponseWrapper
 * @Description
 * @Author fx
 * @date 2026-01-15
 */

public class ResultBean<T> {

    private int code;
    private String msg;
    private T data;
    private String traceId;

    // 私有构造方法，仅允许Builder创建实例
    private ResultBean(Builder<T> builder) {
        this.code = builder.code;
        this.msg = builder.msg;
        this.data = builder.data;
        this.traceId = builder.traceId;
    }

    // 静态内部类：Builder模式（用于构建ResponseWrapper实例）
    public static class Builder<T> {
        // Builder的成员变量（与外部类对应）
        private int code;
        private String msg;
        private T data;
        private String traceId;

        // 链式调用的set方法
        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        // 构建ResponseWrapper实例
        public ResultBean<T> build() {
            return new ResultBean<>(this);
        }
    }

    // Getter方法
    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public T getData() {
        return this.data;
    }

    public String getTraceId() {
        return this.traceId;
    }
}
