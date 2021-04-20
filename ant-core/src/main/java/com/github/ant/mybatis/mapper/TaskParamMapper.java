package com.github.ant.mybatis.mapper;

import com.github.ant.mybatis.model.TaskParam;

public interface TaskParamMapper {
    int deleteByPrimaryKey(Long paramId);

    int insert(TaskParam record);

    int insertSelective(TaskParam record);

    TaskParam selectByPrimaryKey(Long paramId);

    int updateByPrimaryKeySelective(TaskParam record);

    int updateByPrimaryKey(TaskParam record);
}