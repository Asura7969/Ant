package com.github.ant.cluster.worker

import com.github.ant.AntConfig
import com.github.ant.cluster.master.{RegisterWorker, ResponseMsg}
import com.github.ant.internal.Logging
import com.github.ant.rpc.netty.NettyRpcEnvFactory
import com.github.ant.rpc.{RpcAddress, RpcCallContext, RpcConf, RpcEndpoint, RpcEndpointRef, RpcEnv, RpcEnvClientConfig, RpcEnvServerConfig}
import com.github.ant.timer.{SystemTimer, TimeService}

class Worker(antConf: AntConfig) extends Logging {



  val rpcConf = new RpcConf()
  val config = RpcEnvClientConfig(rpcConf, "hello-client")
  val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
  val endPointRef: RpcEndpointRef = rpcEnv.setupEndpointRef(RpcAddress("localhost", 52345), "hello-service")

}

object Worker {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val config = RpcEnvServerConfig(new RpcConf(), "hello-server", host, 52345)
    val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
    val workerEndpoint: RpcEndpoint = new WorkerEndpoint(rpcEnv)
    rpcEnv.setupEndpoint("worker-service", workerEndpoint)
    rpcEnv.awaitTermination()
  }
}

class WorkerEndpoint(antConf: AntConfig,
                     override val rpcEnv: RpcEnv) extends RpcEndpoint with Logging {

  val timeService = new TimeService(
    new SystemTimer("worker-timer",
      antConf.get("worker.timer.tick.ms", "200").toLong,
      antConf.get("worker.timer.wheel.size", "60").toInt)
  )
  // todo:去zk获取 active master信息
  val master: RpcEndpointRef = rpcEnv.setupEndpointRef(
    RpcAddress("localhost", 52345), "master-service")

  override def onStart(): Unit = {
    master.ask[ResponseMsg](RegisterWorker())
  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {

  }

  override def receive: PartialFunction[Any, Unit] = {

  }

  override def onStop(): Unit = {
    timeService.shutdown()
    stop()
  }
}
