package com.github.ant.mybatis.mapper;

import com.github.ant.mybatis.model.TaskRunningInfo;

public interface TaskRunningInfoMapper {
    int deleteByPrimaryKey(Long taskRunningInfoId);

    int insert(TaskRunningInfo record);

    int insertSelective(TaskRunningInfo record);

    TaskRunningInfo selectByPrimaryKey(Long taskRunningInfoId);

    int updateByPrimaryKeySelective(TaskRunningInfo record);

    int updateByPrimaryKey(TaskRunningInfo record);
}