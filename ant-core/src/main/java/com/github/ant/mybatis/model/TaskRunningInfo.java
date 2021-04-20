package com.github.ant.mybatis.model;

import java.util.Date;

public class TaskRunningInfo {
    /**
    * 主键
    */
    private Long taskRunningInfoId;

    /**
    * 任务运行结果状态,0:成功,1:失败
    */
    private String taskRunningInfoStatus;

    /**
    * 任务错误日志
    */
    private byte[] taskRunningInfoError;

    /**
    * 任务运行耗时,单位毫秒
    */
    private Long taskRunningInfoDuration;

    /**
    * 创建时间
    */
    private Date taskRunningInfoCreateTime;

    public Long getTaskRunningInfoId() {
        return taskRunningInfoId;
    }

    public void setTaskRunningInfoId(Long taskRunningInfoId) {
        this.taskRunningInfoId = taskRunningInfoId;
    }

    public String getTaskRunningInfoStatus() {
        return taskRunningInfoStatus;
    }

    public void setTaskRunningInfoStatus(String taskRunningInfoStatus) {
        this.taskRunningInfoStatus = taskRunningInfoStatus;
    }

    public byte[] getTaskRunningInfoError() {
        return taskRunningInfoError;
    }

    public void setTaskRunningInfoError(byte[] taskRunningInfoError) {
        this.taskRunningInfoError = taskRunningInfoError;
    }

    public Long getTaskRunningInfoDuration() {
        return taskRunningInfoDuration;
    }

    public void setTaskRunningInfoDuration(Long taskRunningInfoDuration) {
        this.taskRunningInfoDuration = taskRunningInfoDuration;
    }

    public Date getTaskRunningInfoCreateTime() {
        return taskRunningInfoCreateTime;
    }

    public void setTaskRunningInfoCreateTime(Date taskRunningInfoCreateTime) {
        this.taskRunningInfoCreateTime = taskRunningInfoCreateTime;
    }

    public TaskRunningInfo() {
    }

    public TaskRunningInfo(String taskRunningInfoStatus, byte[] taskRunningInfoError, Long taskRunningInfoDuration, Date taskRunningInfoCreateTime) {
        this.taskRunningInfoStatus = taskRunningInfoStatus;
        this.taskRunningInfoError = taskRunningInfoError;
        this.taskRunningInfoDuration = taskRunningInfoDuration;
        this.taskRunningInfoCreateTime = taskRunningInfoCreateTime;
    }
}