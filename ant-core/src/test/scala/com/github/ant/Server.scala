package com.github.ant

import com.github.ant.rpc.netty.NettyRpcEnvFactory
import com.github.ant.rpc.{RpcCallContext, RpcConf, RpcEndpoint, RpcEnv, RpcEnvServerConfig}

object Server {
  def main(args: Array[String]): Unit = {
    //    val host = args(0)
    val host = "localhost"
    val config = RpcEnvServerConfig(new RpcConf(), "hello-server", host, 52345)
    val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
    val helloEndpoint: RpcEndpoint = new HelloEndpoint(rpcEnv)
    rpcEnv.setupEndpoint("hello-service", helloEndpoint)
    rpcEnv.awaitTermination()
  }
}

class HelloEndpoint(override val rpcEnv: RpcEnv) extends RpcEndpoint {

  override def onStart(): Unit = {
    println("start hello endpoint")
  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    case SayHi(msg) =>
      //println(s"receive $msg")
      context.reply(s"$msg")
    case SayBye(msg) =>
      //println(s"receive $msg")
      context.reply(s"bye, $msg")
  }

  override def receive: PartialFunction[Any, Unit] = {
    case OnlyReceiveMessage(msg) =>
      println(s"server receive: $msg")
  }

  override def onStop(): Unit = {
    println("stop hello endpoint")
  }
}
case class OnlyReceiveMessage(msg: String)

case class SayHi(msg: String)

case class SayBye(msg: String)