package com.github.ant.network.protocol.message;

import com.github.ant.network.protocol.AbstractMessage;
import com.github.ant.network.protocol.ResponseMessage;
import io.netty.buffer.ByteBuf;

/**
 * Master节点通知worker节点上报task信息,校验任务
 */
public final class Rebalance extends AbstractMessage implements ResponseMessage {
    @Override
    public Type type() {
        return Type.Rebalance;
    }

    @Override
    public int encodedLength() {
        return 0;
    }

    @Override
    public void encode(ByteBuf buf) {
        // do nothing
    }

    public static Rebalance decode(ByteBuf buf) {
        return new Rebalance();
    }
}
