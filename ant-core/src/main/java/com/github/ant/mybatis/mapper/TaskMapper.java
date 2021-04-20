package com.github.ant.mybatis.mapper;

import com.github.ant.mybatis.model.Task;

import java.util.List;

public interface TaskMapper {
    int deleteByPrimaryKey(Long taskId);

    int insert(Task record);

    int insertSelective(Task record);

    Task selectByPrimaryKey(Long taskId);

    int updateByPrimaryKeySelective(Task record);

    int updateByPrimaryKey(Task record);

    List<Task> getTaskListByAddress(String address);
}