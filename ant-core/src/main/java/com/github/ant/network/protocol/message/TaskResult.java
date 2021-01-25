package com.github.ant.network.protocol.message;

import com.github.ant.network.protocol.AbstractMessage;
import com.github.ant.network.protocol.Encoders;
import com.github.ant.network.protocol.ResponseMessage;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

/**
 * 任务调度状态、运行状态
 */
public final class TaskResult extends AbstractMessage implements ResponseMessage {
    private final long taskId;
    /**
     * 1、任务调度（成功失败）
     * task schedule success: 0
     * task schedule fail:   -1
     * 2、任务正在运行
     * task running:          1
     * 3、任务执行（成功失败）
     * task success:          2
     * task fail:             3
     */
    private final int status;
    private final long taskPid;
    private final String errorCause;

    public TaskResult(long taskId, int status, long taskPid, String errorCause){
        this.taskId = taskId;
        this.status = status;
        this.taskPid = taskPid;
        this.errorCause = errorCause;
    }

    public TaskResult(long taskId, long taskPid){
        this.taskId = taskId;
        this.status = 1;
        this.taskPid = taskPid;
        this.errorCause = null;
    }

    @Override
    public int encodedLength() {
        // taskId + status + taskPid + errorCause
        return 8 + 4 + 8 + (Objects.isNull(errorCause) ? 0 : Encoders.Strings.encodedLength(errorCause));
    }


    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(taskId);
        buf.writeInt(status);
        buf.writeLong(taskPid);
        if(null != errorCause) {
            Encoders.Strings.encode(buf, errorCause);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("taskId", taskId)
                .append("status", status)
                .append("taskPid", taskPid)
                .append("errorCause", errorCause)
                .toString();
    }

    @Override
    public Type type() {
        return Type.TaskResult;
    }

    public static TaskResult decode(ByteBuf buf) {
        buf.readInt();
        long taskId = buf.readLong();
        int status = buf.readInt();
        long taskPid = buf.readLong();
        String errorCause = null;
        if(buf.isReadable()){
            errorCause = Encoders.Strings.decode(buf);
        }
        return new TaskResult(taskId, status, taskPid, errorCause);
    }
}
