package com.github.ant.cluster.worker

import com.github.ant.{AntConfig, DynamicParameters, GlobalConfig}
import com.github.ant.DynamicParameters._
import com.github.ant.cluster.master.{AssignTaskInfo, ChangeMaster, DeleteJob, Fail, GetTask, RegisterWorker, ResponseMsg, Success}
import com.github.ant.internal.Utils._
import com.github.ant.internal.{Logging, Utils}
import com.github.ant.network.protocol.message.TaskInfo
import com.github.ant.rpc.netty.NettyRpcEnvFactory
import com.github.ant.rpc.{RpcAddress, RpcCallContext, RpcConf, RpcEndpoint, RpcEndpointRef, RpcEnv, RpcEnvClientConfig, RpcEnvServerConfig}
import com.github.ant.timer.{SystemTimer, TimeService}
import com.github.ant.utils.ParameterTool

import scala.concurrent.ExecutionContext.Implicits.global


class Worker(antConf: AntConfig) extends Logging {

}

object Worker {
  def main(args: Array[String]): Unit = {
    // todo: 动态参数
    val tool = ParameterTool.fromArgs(args)
    val globalConfig = new GlobalConfig(Map("ANT_CONF_DIR" -> System.getenv("ANT_CONF_DIR")))
    val conf = globalConfig.toAntConfig
    val (host, port) = conf.getLocalServerInfo("ant.cluster.worker.address")
    val localhost = getLocalAddress
    val config = RpcEnvServerConfig(globalConfig.toRpcConfig, s"$host:$port", localhost, port)
    val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
    conf.set(ACTIVE_MASTER_URL, args(0))
    val workerEndpoint: RpcEndpoint = new WorkerEndpoint(conf, rpcEnv)
    rpcEnv.setupEndpoint(rpcEnv.address.hostPort, workerEndpoint)
    rpcEnv.awaitTermination()
  }
}

class WorkerEndpoint(antConf: AntConfig,
                     override val rpcEnv: RpcEnv) extends RpcEndpoint with Logging {

  import WorkerEndpoint._

  val timeService = new TimeService(
    new SystemTimer("worker-timer",
      antConf.get("worker.timer.tick.ms", "200").toLong,
      antConf.get("worker.timer.wheel.size", "60").toInt)
  )

  var master: RpcEndpointRef = {
    val masterUrl = antConf.get(ACTIVE_MASTER_URL)
    val (host, port) = Utils.extractHostPortFromAntUrl(masterUrl)
    rpcEnv.setupEndpointRef(
      RpcAddress.fromAntURL(masterUrl), s"$host:$port")
  }

  override def onStart(): Unit = {
    // start 之后向master注册本节点信息
    val host = rpcEnv.address.host
    val port = rpcEnv.address.port
    master.ask[ResponseMsg](RegisterWorker(host, port)).onComplete {
      case scala.util.Success(_) =>
        logInfo(s"Worker($host:$port) registered successfully!")
      case scala.util.Failure(e) =>
        logError(s"Worker($host:$port) registered failed!")
        stop()
        return
    }

    // todo: 开启心跳线程，定时向 master 上报心跳信息

  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    handleMsg(context)
  }

  override def receive: PartialFunction[Any, Unit] = {
    handleMsg(null)
  }

  override def onStop(): Unit = {
    timeService.shutdown()
    stop()
  }

  def handleMsg(ctx: RpcCallContext): PartialFunction[Any, Unit] = {
    case Success =>
    // do nothing
    case AssignTaskInfo(taskId, cronExpression, taskParam) =>
      processThrowable(ctx,
        timeService.addTask(new TaskInfo(taskId, cronExpression, taskParam)))

    case DeleteJob(id) =>
      processThrowable(ctx, timeService.removeTask(id))

    case GetTask(optionId) =>
      val result = optionId match {
        case Some(taskId) =>
          timeService.getTask(taskId)
        case None =>
          timeService.getAllTask
      }
      ctx.reply(result)

    case ChangeMaster(address, port) =>
      // todo: 去zk获取当前存活的master
      master = rpcEnv.setupEndpointRef(
        RpcAddress(address, port), "master-service")
      ctx.reply(Success())
  }


}

object WorkerEndpoint {
  private def processThrowable(ctx: RpcCallContext, option: Option[Throwable]): Unit = {
    option match {
      case Some(ex) => ctx.sendFailure(ex)
      case None => ctx.reply(Success())
    }
  }
}