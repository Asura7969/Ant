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

package com.github.ant.network;

import com.github.ant.network.client.TransportClient;
import com.github.ant.network.client.TransportClientFactory;
import com.github.ant.network.client.TransportResponseHandler;
import com.github.ant.network.protocol.MessageDecoder;
import com.github.ant.network.protocol.MessageEncoder;
import com.github.ant.network.server.RpcHandler;
import com.github.ant.network.server.TransportChannelHandler;
import com.github.ant.network.server.TransportRequestHandler;
import com.github.ant.network.server.TransportServer;
import com.github.ant.utils.IOMode;
import com.github.ant.utils.NettyUtils;
import com.github.ant.utils.TransportConf;
import com.github.ant.utils.TransportFrameDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class TransportContext implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(TransportContext.class);

  private final TransportConf conf;
  private final RpcHandler rpcHandler;
  private final boolean closeIdleConnections;
  // Number of registered connections to the shuffle service
  /**
   * Force to create MessageEncoder and MessageDecoder so that we can make sure they will be created
   * before switching the current context class loader to ExecutorClassLoader.
   *
   * Netty's MessageToMessageEncoder uses Javassist to generate a matcher class and the
   * implementation calls "Class.forName" to check if this calls is already generated. If the
   * following two objects are created in "ExecutorClassLoader.findClass", it will cause
   * "ClassCircularityError". This is because loading this Netty generated class will call
   * "ExecutorClassLoader.findClass" to search this class, and "ExecutorClassLoader" will try to use
   * RPC to load it and cause to load the non-exist matcher class again. JVM will report
   * `ClassCircularityError` to prevent such infinite recursion. (See SPARK-17714)
   */
  private static final MessageEncoder ENCODER = MessageEncoder.INSTANCE;
  private static final MessageDecoder DECODER = MessageDecoder.INSTANCE;

  // Separate thread pool for handling ChunkFetchRequest. This helps to enable throttling
  // max number of TransportServer worker threads that are blocked on writing response
  // of ChunkFetchRequest message back to the client via the underlying channel.
  private final EventLoopGroup chunkFetchWorkers;

  public TransportContext(TransportConf conf, RpcHandler rpcHandler) {
    this(conf, rpcHandler, false, false);
  }

  public TransportContext(
      TransportConf conf,
      RpcHandler rpcHandler,
      boolean closeIdleConnections) {
    this(conf, rpcHandler, closeIdleConnections, false);
  }

  /**
   * Enables TransportContext initialization for underlying client and server.
   *
   * @param conf TransportConf
   * @param rpcHandler RpcHandler responsible for handling requests and responses.
   * @param closeIdleConnections Close idle connections if it is set to true.
   * @param isClientOnly This config indicates the TransportContext is only used by a client.
   *                     This config is more important when external shuffle is enabled.
   *                     It stops creating extra event loop and subsequent thread pool
   *                     for shuffle clients to handle chunked fetch requests.
   */
  public TransportContext(
      TransportConf conf,
      RpcHandler rpcHandler,
      boolean closeIdleConnections,
      boolean isClientOnly) {
    this.conf = conf;
    this.rpcHandler = rpcHandler;
    this.closeIdleConnections = closeIdleConnections;

    if (conf.getModuleName() != null &&
        conf.getModuleName().equalsIgnoreCase("shuffle") &&
        !isClientOnly && conf.separateChunkFetchRequest()) {
//      chunkFetchWorkers = NettyUtils.createEventLoop(
//          IOMode.valueOf(conf.ioMode()),
//          conf.chunkFetchHandlerThreads(),
//          "shuffle-chunk-fetch-handler");
      chunkFetchWorkers = null;
    } else {
      chunkFetchWorkers = null;
    }
  }

  public TransportClientFactory createClientFactory() {
    return new TransportClientFactory(this);
  }

  /** Create a server which will attempt to bind to a specific port. */
  public TransportServer createServer(int port) {
    return new TransportServer(this, null, port, rpcHandler);
  }

  /** Create a server which will attempt to bind to a specific host and port. */
  public TransportServer createServer(
      String host, int port) {
    return new TransportServer(this, host, port, rpcHandler);
  }

  /** Creates a new server, binding to any available ephemeral port. */

  public TransportServer createServer() {
    return createServer(0);
  }

  public TransportChannelHandler initializePipeline(SocketChannel channel) {
    return initializePipeline(channel, rpcHandler);
  }

  public TransportChannelHandler initializePipeline(
      SocketChannel channel,
      RpcHandler channelRpcHandler) {
    try {
      TransportChannelHandler channelHandler = createChannelHandler(channel, channelRpcHandler);
      ChannelPipeline pipeline = channel.pipeline()
        .addLast("encoder", ENCODER)
        .addLast(TransportFrameDecoder.HANDLER_NAME, NettyUtils.createFrameDecoder())
        .addLast("decoder", DECODER)
        .addLast("idleStateHandler",
          new IdleStateHandler(0, 0, conf.connectionTimeoutMs() / 1000))
        // NOTE: Chunks are currently guaranteed to be returned in the order of request, but this
        // would require more logic to guarantee if this were not part of the same event loop.
        .addLast("handler", channelHandler);
      return channelHandler;
    } catch (RuntimeException e) {
      logger.error("Error while initializing Netty pipeline", e);
      throw e;
    }
  }

  /**
   * Creates the server- and client-side handler which is used to handle both RequestMessages and
   * ResponseMessages. The channel is expected to have been successfully created, though certain
   * properties (such as the remoteAddress()) may not be available yet.
   */
  private TransportChannelHandler createChannelHandler(Channel channel, RpcHandler rpcHandler) {
    TransportResponseHandler responseHandler = new TransportResponseHandler(channel);
    TransportClient client = new TransportClient(channel, responseHandler);
    boolean separateChunkFetchRequest = conf.separateChunkFetchRequest();
    TransportRequestHandler requestHandler = new TransportRequestHandler(channel, client,
      rpcHandler, conf.maxChunksBeingTransferred());
    return new TransportChannelHandler(client, responseHandler, requestHandler,
      conf.connectionTimeoutMs(), separateChunkFetchRequest, closeIdleConnections, this);
  }

  public TransportConf getConf() { return conf; }

  public void close() {
    if (chunkFetchWorkers != null) {
      chunkFetchWorkers.shutdownGracefully();
    }
  }
}
