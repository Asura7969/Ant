package com.github.ant.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ant.network.protocol.Encoders;
import io.netty.buffer.ByteBuf;

public abstract class TaskParam<T> {
    protected ObjectMapper mapper = new ObjectMapper();

    public void encode(ByteBuf buf) throws JsonProcessingException {
        Encoders.Strings.encode(buf, mapper.writeValueAsString(this));
    }

    public T decode(ByteBuf buf) throws JsonProcessingException {
        return (T) mapper.readValue(Encoders.Strings.decode(buf), this.getClass());
    }
}
