package com.github.ant.mybatis.model;

import java.util.Date;
public class Task {
    /**
    * taskId,主键
    */
    private Long taskId;

    /**
    * task名称
    */
    private String taskName;

    /**
    * 任务状态,0:禁用,1:启用,2:删除, 3:准备
    */
    private String taskStatus = "3";

    /**
    * 创建时间
    */
    private Date taskCreateTime;

    /**
    * 最近一次修改时间
    */
    private Date taskUpdateTime = new Date();

    /**
    * 任务参数id,与任务关联
    */
    private Long taskParamId;

    /**
    * 任务文件id,与任务关联
    */
    private Long taskFileId;

    /**
    * 任务运行命令
    */
    private String taskCommand;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Date getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Date taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Date getTaskUpdateTime() {
        return taskUpdateTime;
    }

    public void setTaskUpdateTime(Date taskUpdateTime) {
        this.taskUpdateTime = taskUpdateTime;
    }

    public Long getTaskParamId() {
        return taskParamId;
    }

    public void setTaskParamId(Long taskParamId) {
        this.taskParamId = taskParamId;
    }

    public Long getTaskFileId() {
        return taskFileId;
    }

    public void setTaskFileId(Long taskFileId) {
        this.taskFileId = taskFileId;
    }

    public String getTaskCommand() {
        return taskCommand;
    }

    public void setTaskCommand(String taskCommand) {
        this.taskCommand = taskCommand;
    }

    public Task() {
    }

    public Task(String taskName, String taskStatus, Date taskCreateTime, Long taskParamId, Long taskFileId, String taskCommand) {
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.taskCreateTime = taskCreateTime;
        this.taskParamId = taskParamId;
        this.taskFileId = taskFileId;
        this.taskCommand = taskCommand;
    }

    public Task(String taskName, Date taskCreateTime, Long taskParamId, Long taskFileId, String taskCommand) {
        this.taskName = taskName;
        this.taskCreateTime = taskCreateTime;
        this.taskParamId = taskParamId;
        this.taskFileId = taskFileId;
        this.taskCommand = taskCommand;
    }
}