package com.github.ant.network.protocol.message;

import com.github.ant.network.protocol.AbstractMessage;
import com.github.ant.network.protocol.Encoders;
import com.github.ant.network.protocol.ResponseMessage;
import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class RegisterExecutor extends AbstractMessage implements ResponseMessage {
    private String ip;
    private long port;

    public RegisterExecutor(String ip, long port) {
        this.ip = ip;
        this.port = port;
    }

    public static RegisterExecutor createRegisterExecutorInfo(int port) throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();;
        String ip = addr.getHostAddress();
        return new RegisterExecutor(ip, port);
    }
    @Override
    public Type type() {
        return Type.RegisterExecutor;
    }

    @Override
    public int encodedLength() {
        return Encoders.Strings.encodedLength(ip) + 4;
    }

    @Override
    public void encode(ByteBuf buf) {
        Encoders.Strings.encode(buf, ip);
        buf.writeLong(port);
    }

    public static RegisterExecutor decode(ByteBuf buf) {
        buf.readInt();
        String ip = Encoders.Strings.decode(buf);
        long port = buf.readLong();
        return new RegisterExecutor(ip, port);
    }
}
