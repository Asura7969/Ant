package com.github.ant.network.protocol.message;

import com.github.ant.network.protocol.AbstractMessage;
import com.github.ant.network.protocol.Encoders;
import com.github.ant.network.protocol.RequestMessage;
import io.netty.buffer.ByteBuf;

/**
 * task 任务信息
 */
public final class TaskInfo extends AbstractMessage implements RequestMessage {
    private final long taskId;
    private final String cronExpression;

    public TaskInfo(long taskId, String cronExpression){
        this.taskId = taskId;
        this.cronExpression = cronExpression;
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
    }

    public static TaskInfo decode(ByteBuf buf) {
        buf.readInt();
        long taskId = buf.readLong();
        String cronExpression = Encoders.Strings.decode(buf);
        return new TaskInfo(taskId, cronExpression);
    }
}
