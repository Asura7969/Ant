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

package com.github.ant.network.protocol;

import com.github.ant.network.buffer.ManagedBuffer;
import io.netty.buffer.ByteBuf;

/** An on-the-wire transmittable message. */
public interface Message extends Encodable {
  /** Used to identify this request type. */
  Type type();

  /** An optional body for the message. */
  ManagedBuffer body();

  /** Whether to include the body of the message in the same frame as the message. */
  boolean isBodyInFrame();

  /** Preceding every serialized Message is its type, which allows us to deserialize it. */
  enum Type implements Encodable {

    Heartbeat(0),
    MigrateTask(1),
    Rebalance(2),
    TaskResult(3),
    TaskInfo(4),
    VerificationTask(5),
    RegisterExecutor(6),
    OneWayMessage(7),
    RpcRequest(8),
    RpcResponse(9),
    RpcFailure(10);

//    ChunkFetchRequest(0), ChunkFetchSuccess(1), ChunkFetchFailure(2),
//    RpcRequest(3), RpcResponse(4), RpcFailure(5),
//    StreamRequest(6), StreamResponse(7), StreamFailure(8),
//    OneWayMessage(9), UploadStream(10), User(-1);

    private final byte id;

    Type(int id) {
      assert id < 128 : "Cannot have more than 128 message types";
      this.id = (byte) id;
    }

    public byte id() { return id; }

    @Override public int encodedLength() { return 1; }

    @Override public void encode(ByteBuf buf) { buf.writeByte(id); }

    public static Type decode(ByteBuf buf) {
      byte id = buf.readByte();
      switch (id) {
        case 0: return Heartbeat;
        case 1: return MigrateTask;
        case 2: return Rebalance;
        case 3: return TaskResult;
        case 4: return TaskInfo;
        case 5: return VerificationTask;
        case 6: return RegisterExecutor;
        case 7: return OneWayMessage;
        case 8: return RpcRequest;
        case 9: return RpcResponse;
        case 10: return RpcFailure;
//        case -1: throw new IllegalArgumentException("User type messages cannot be decoded.");
        default: throw new IllegalArgumentException("Unknown message type: " + id);
      }
    }
  }
}
