package com.github.ant.network.protocol.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ant.job.HttpTask;
import com.github.ant.job.ScribeTask;
import com.github.ant.job.SoaRpcTask;
import com.github.ant.job.TaskParam;
import com.github.ant.network.protocol.AbstractMessage;
import com.github.ant.network.protocol.Encoders;
import com.github.ant.network.protocol.RequestMessage;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * task 任务信息
 */
public class TaskInfo extends AbstractMessage implements RequestMessage {
    private final long taskId;
    private final String cronExpression;
    private TaskParam taskParam;

    public TaskInfo(long taskId, String cronExpression, TaskParam taskParam){
        this.taskId = taskId;
        this.cronExpression = cronExpression;
        this.taskParam = taskParam;
    }

    public TaskInfo(long taskId, String cronExpression){
        this.taskId = taskId;
        this.cronExpression = cronExpression;
    }

    public long getTaskId() {
        return this.taskId;
    }

    @Override
    public Type type() {
        return Type.TaskInfo;
    }

    @Override
    public int encodedLength() {
        return 8 + Encoders.Strings.encodedLength(cronExpression);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(taskId);
        Encoders.Strings.encode(buf, cronExpression);
        try {
            taskParam.encode(buf);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static TaskInfo decode(ByteBuf buf) {
        buf.readInt();
        long taskId = buf.readLong();
        String cronExpression = Encoders.Strings.decode(buf);
        try {
            return new TaskInfo(taskId, cronExpression, TaskParam.decode(buf));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new TaskInfo(taskId, cronExpression);
    }

    public TaskParam getTaskParam() {
        return taskParam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskInfo taskInfo = (TaskInfo) o;
        return taskId == taskInfo.taskId &&
                cronExpression.equals(taskInfo.cronExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, cronExpression);
    }
}
