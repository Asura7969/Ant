package com.github.ant.mybatis.model;

import java.util.Date;

public class TaskParam {
    /**
    * 主键
    */
    private Long paramId;

    /**
    * 关联的文件id
    */
    private Long paramFileId;

    /**
    * 创建时间
    */
    private Date paramCreateTime;

    /**
    * 最近一次修改时间
    */
    private Date paramUpdateTime = new Date();

    public Long getParamId() {
        return paramId;
    }

    public void setParamId(Long paramId) {
        this.paramId = paramId;
    }

    public Long getParamFileId() {
        return paramFileId;
    }

    public void setParamFileId(Long paramFileId) {
        this.paramFileId = paramFileId;
    }

    public Date getParamCreateTime() {
        return paramCreateTime;
    }

    public void setParamCreateTime(Date paramCreateTime) {
        this.paramCreateTime = paramCreateTime;
    }

    public Date getParamUpdateTime() {
        return paramUpdateTime;
    }

    public void setParamUpdateTime(Date paramUpdateTime) {
        this.paramUpdateTime = paramUpdateTime;
    }

    public TaskParam(Long paramFileId, Date paramCreateTime, Date paramUpdateTime) {
        this.paramFileId = paramFileId;
        this.paramCreateTime = paramCreateTime;
        this.paramUpdateTime = paramUpdateTime;
    }

    public TaskParam() {
    }

    public TaskParam(Date paramCreateTime) {
        this.paramCreateTime = paramCreateTime;
    }
}