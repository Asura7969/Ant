package com.github.ant.mybatis.mapper;

import com.github.ant.mybatis.model.TaskRuntime;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

public interface TaskRuntimeMapper {
    int deleteByPrimaryKey(Long runtimeId);

    int insert(TaskRuntime record);

    int insertSelective(TaskRuntime record);

    TaskRuntime selectByPrimaryKey(Long runtimeId);

    int updateByPrimaryKeySelective(TaskRuntime record);

    int updateByPrimaryKey(TaskRuntime record);

    List<TaskRuntime> selectAddressAndTaskId();
}