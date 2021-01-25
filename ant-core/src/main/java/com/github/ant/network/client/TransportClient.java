/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ant.network.client;

import com.github.ant.network.buffer.NioManagedBuffer;
import com.github.ant.network.protocol.RpcRequest;
import com.github.ant.network.protocol.message.OneWayMessage;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.ant.utils.NettyUtils.getRemoteAddress;

public class TransportClient implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(TransportClient.class);

  private final Channel channel;
  private final TransportResponseHandler handler;
  @Nullable private String clientId;
  private volatile boolean timedOut;

  public TransportClient(Channel channel, TransportResponseHandler handler) {
    this.channel = Preconditions.checkNotNull(channel);
    this.handler = Preconditions.checkNotNull(handler);
    this.timedOut = false;
  }

  public Channel getChannel() {
    return channel;
  }

  public boolean isActive() {
    return !timedOut && (channel.isOpen() || channel.isActive());
  }

  public SocketAddress getSocketAddress() {
    return channel.remoteAddress();
  }

  /**
   * Returns the ID used by the client to authenticate itself when authentication is enabled.
   *
   * @return The client ID, or null if authentication is disabled.
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Sets the authenticated client ID. This is meant to be used by the authentication layer.
   *
   * Trying to set a different client ID after it's been set will result in an exception.
   */
  public void setClientId(String id) {
    Preconditions.checkState(clientId == null, "Client ID has already been set.");
    this.clientId = id;
  }

  /**
   * Sends an opaque message to the RpcHandler on the server-side. The callback will be invoked
   * with the server's response or upon any failure.
   *
   * @param message The message to send.
   * @param callback Callback to handle the RPC's reply.
   * @return The RPC's id.
   */
  public long sendRpc(ByteBuffer message, RpcResponseCallback callback) {
    if (logger.isTraceEnabled()) {
      logger.trace("Sending RPC to {}", getRemoteAddress(channel));
    }

    long requestId = requestId();
    handler.addRpcRequest(requestId, callback);

    RpcChannelListener listener = new RpcChannelListener(requestId, callback);
    channel.writeAndFlush(new RpcRequest(requestId, new NioManagedBuffer(message)))
      .addListener(listener);

    return requestId;
  }
  /**
   * Synchronously sends an opaque message to the RpcHandler on the server-side, waiting for up to
   * a specified timeout for a response.
   */
  public ByteBuffer sendRpcSync(ByteBuffer message, long timeoutMs) {
    final SettableFuture<ByteBuffer> result = SettableFuture.create();

    sendRpc(message, new RpcResponseCallback() {
      @Override
      public void onSuccess(ByteBuffer response) {
        try {
          ByteBuffer copy = ByteBuffer.allocate(response.remaining());
          copy.put(response);
          // flip "copy" to make it readable
          copy.flip();
          result.set(copy);
        } catch (Throwable t) {
          logger.warn("Error in responding PRC callback", t);
          result.setException(t);
        }
      }

      @Override
      public void onFailure(Throwable e) {
        result.setException(e);
      }
    });

    try {
      return result.get(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw Throwables.propagate(e.getCause());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Sends an opaque message to the RpcHandler on the server-side. No reply is expected for the
   * message, and no delivery guarantees are made.
   *
   * @param message The message to send.
   */
  public void send(ByteBuffer message) {
    channel.writeAndFlush(new OneWayMessage(new NioManagedBuffer(message)));
  }

  /**
   * Removes any state associated with the given RPC.
   *
   * @param requestId The RPC id returned by {@link #sendRpc(ByteBuffer, RpcResponseCallback)}.
   */
  public void removeRpcRequest(long requestId) {
    handler.removeRpcRequest(requestId);
  }

  /** Mark this channel as having timed out. */
  public void timeOut() {
    this.timedOut = true;
  }

  @VisibleForTesting
  public TransportResponseHandler getHandler() {
    return handler;
  }

  @Override
  public void close() {
    // close is a local operation and should finish with milliseconds; timeout just to be safe
    channel.close().awaitUninterruptibly(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("remoteAdress", channel.remoteAddress())
      .append("clientId", clientId)
      .append("isActive", isActive())
      .toString();
  }

  private static long requestId() {
    return Math.abs(UUID.randomUUID().getLeastSignificantBits());
  }

  private class StdChannelListener
      implements GenericFutureListener<Future<? super Void>> {
    final long startTime;
    final Object requestId;

    StdChannelListener(Object requestId) {
      this.startTime = System.currentTimeMillis();
      this.requestId = requestId;
    }

    @Override
    public void operationComplete(Future<? super Void> future) throws Exception {
      if (future.isSuccess()) {
        if (logger.isTraceEnabled()) {
          long timeTaken = System.currentTimeMillis() - startTime;
          logger.trace("Sending request {} to {} took {} ms", requestId,
              getRemoteAddress(channel), timeTaken);
        }
      } else {
        String errorMsg = String.format("Failed to send RPC %s to %s: %s", requestId,
            getRemoteAddress(channel), future.cause());
        logger.error(errorMsg, future.cause());
        channel.close();
        try {
          handleFailure(errorMsg, future.cause());
        } catch (Exception e) {
          logger.error("Uncaught exception in RPC response callback handler!", e);
        }
      }
    }

    void handleFailure(String errorMsg, Throwable cause) throws Exception {}
  }

  private class RpcChannelListener extends StdChannelListener {
    final long rpcRequestId;
    final RpcResponseCallback callback;

    RpcChannelListener(long rpcRequestId, RpcResponseCallback callback) {
      super("RPC " + rpcRequestId);
      this.rpcRequestId = rpcRequestId;
      this.callback = callback;
    }

    @Override
    void handleFailure(String errorMsg, Throwable cause) {
      handler.removeRpcRequest(rpcRequestId);
      callback.onFailure(new IOException(errorMsg, cause));
    }
  }

}
