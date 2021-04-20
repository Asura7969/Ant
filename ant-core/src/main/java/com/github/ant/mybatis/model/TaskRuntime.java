package com.github.ant.mybatis.model;

import java.util.Objects;

public class TaskRuntime {
    /**
    * 主键
    */
    private Long runtimeId;

    /**
    * 任务id
    */
    private Long runtimeTaskId;

    /**
    * 任务部署运行地址
    */
    private String runtimeAddress;

    public Long getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(Long runtimeId) {
        this.runtimeId = runtimeId;
    }

    public Long getRuntimeTaskId() {
        return runtimeTaskId;
    }

    public void setRuntimeTaskId(Long runtimeTaskId) {
        this.runtimeTaskId = runtimeTaskId;
    }

    public String getRuntimeAddress() {
        return runtimeAddress;
    }

    public void setRuntimeAddress(String runtimeAddress) {
        this.runtimeAddress = runtimeAddress;
    }

    public TaskRuntime() {
    }

    public TaskRuntime(Long runtimeTaskId, String runtimeAddress) {
        this.runtimeTaskId = runtimeTaskId;
        this.runtimeAddress = runtimeAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRuntime that = (TaskRuntime) o;
        return Objects.equals(runtimeTaskId, that.runtimeTaskId) &&
                Objects.equals(runtimeAddress, that.runtimeAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runtimeTaskId, runtimeAddress);
    }
}