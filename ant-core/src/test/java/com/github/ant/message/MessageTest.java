package com.github.ant.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ant.job.HttpTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

public class MessageTest {

    @Test
    public void encodeTest() throws JsonProcessingException {
        HttpTask httpTask = new HttpTask("get", "http://localhost:8080", "param", null, true, 1000L);
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        httpTask.encode(buf);
        HttpTask decode = httpTask.decode(buf);
        assert decode.equals(httpTask);
        assert decode.hashCode() == httpTask.hashCode();

    }
}
