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

package com.github.ant.util

import com.github.ant.function.NotFoundException
import com.github.ant.internal.Utils
import com.github.ant.internal.Utils.getLocalAddress
import com.github.ant.rpc.{RpcAddress, RpcConf, RpcEndpointRef, RpcEnv, RpcTimeout}

import scala.concurrent.duration._

object RpcUtils {

  /**
   * Retrieve a `RpcEndpointRef` which is located in the driver via its name.
   */
  def makeMasterRef(name: String, conf: RpcConf, rpcEnv: RpcEnv): RpcEndpointRef = {
    val driverAddress: String = conf.get("ant.cluster.master.address",
      s"$getLocalAddress:7077")

    driverAddress.split(",").find(address => address.startsWith(s"$getLocalAddress:")) match {
      case Some(address) =>
        val addressString = address.split(":")
        rpcEnv.setupEndpointRef(RpcAddress(addressString(0), addressString(1).toInt), name)

      case _ => throw new NotFoundException(s"Not found address like $getLocalAddress:%")
    }
  }

  /** Returns the configured number of times to retry connecting */
  def numRetries(conf: RpcConf): Int = {
    conf.getInt("ant.rpc.numRetries", 3)
  }

  /** Returns the configured number of milliseconds to wait on each retry */
  def retryWaitMs(conf: RpcConf): Long = {
    conf.getTimeAsMs("ant.rpc.retry.wait", "3s")
  }

  /** Returns the default Ant timeout to use for RPC ask operations. */
  def askRpcTimeout(conf: RpcConf): RpcTimeout = {
    RpcTimeout(conf, Seq("ant.rpc.askTimeout", "ant.rpc.network.timeout"), "120s")
  }

  /** Returns the default Ant timeout to use for RPC remote endpoint lookup. */
  def lookupRpcTimeout(conf: RpcConf): RpcTimeout = {
    RpcTimeout(conf, Seq("ant.rpc.lookupTimeout", "ant.rpc.network.timeout"), "120s")
  }

  private val MAX_MESSAGE_SIZE_IN_MB = Int.MaxValue / 1024 / 1024

  /** Returns the configured max message size for messages in bytes. */
  def maxMessageSizeBytes(conf: RpcConf): Int = {
    val maxSizeInMB = conf.getInt("ant.rpc.message.maxSize", 128)
    if (maxSizeInMB > MAX_MESSAGE_SIZE_IN_MB) {
      throw new IllegalArgumentException(
        s"ant.rpc.message.maxSize should not be greater than $MAX_MESSAGE_SIZE_IN_MB MB")
    }
    maxSizeInMB * 1024 * 1024
  }
}
