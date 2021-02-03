package com.github.ant.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ant.network.protocol.Encoders;
import io.netty.buffer.ByteBuf;

public abstract class TaskParam {
    protected static ObjectMapper mapper = new ObjectMapper();

    public void encode(ByteBuf buf) throws JsonProcessingException {
        buf.writeInt(getType().id);
        System.out.println("getType id is: " + getType().id);
        Encoders.Strings.encode(buf, mapper.writeValueAsString(this));
    }

    public static TaskParam decode(ByteBuf buf) throws JsonProcessingException {
        int len = buf.readInt();
        return mapper.readValue(Encoders.Strings.decode(buf), getClassType(len));
    }

    private static Class<? extends TaskParam> getClassType(int taskType) {
        switch (taskType){
            case 0:
                return HttpTask.class;
            case 1:
                return ScribeTask.class;
            case 2:
                return SoaRpcTask.class;
            default:
                System.out.println("taskType is: " + taskType);
                return null;
        }
    }

    public abstract TaskType getType();

    public enum TaskType {
        HTTP(0),
        SCRIBE(1),
        SOA_RPC(2);

        private int id;

        TaskType(int id){
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
