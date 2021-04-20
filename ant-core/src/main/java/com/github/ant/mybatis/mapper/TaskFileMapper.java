package com.github.ant.mybatis.mapper;

import com.github.ant.mybatis.model.TaskFile;

public interface TaskFileMapper {
    int deleteByPrimaryKey(Long fileId);

    int insert(TaskFile record);

    int insertSelective(TaskFile record);

    TaskFile selectByPrimaryKey(Long fileId);

    int updateByPrimaryKeySelective(TaskFile record);

    int updateByPrimaryKey(TaskFile record);
}