package com.github.ant.mybatis.model;

import java.util.Date;

public class TaskFile {
    /**
    * 主键
    */
    private Long fileId;

    /**
    * 文件类型
    */
    private String fileType;

    /**
    * 文件内容,二进制存储
    */
    private String fileContent;

    /**
    * 创建时间
    */
    private Date fileCreateTime;

    /**
    * 最近一次修改时间
    */
    private Date fileUpdateTime;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public Date getFileCreateTime() {
        return fileCreateTime;
    }

    public void setFileCreateTime(Date fileCreateTime) {
        this.fileCreateTime = fileCreateTime;
    }

    public Date getFileUpdateTime() {
        return fileUpdateTime;
    }

    public void setFileUpdateTime(Date fileUpdateTime) {
        this.fileUpdateTime = fileUpdateTime;
    }

    public TaskFile() {
    }

    public TaskFile(String fileType, String fileContent, Date fileCreateTime, Date fileUpdateTime) {
        this.fileType = fileType;
        this.fileContent = fileContent;
        this.fileCreateTime = fileCreateTime;
        this.fileUpdateTime = fileUpdateTime;
    }
}