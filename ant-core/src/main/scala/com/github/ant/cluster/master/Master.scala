package com.github.ant.cluster.master

import java.util
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import com.github.ant.AntConfig
import com.github.ant.function.ExistsException
import com.github.ant.internal.Logging
import com.github.ant.job.{HttpTask, ScribeTask, SoaRpcTask}
import com.github.ant.network.protocol.message.TaskInfo
import com.github.ant.rpc.netty.NettyRpcEnvFactory
import com.github.ant.rpc.{RpcAddress, RpcCallContext, RpcConf, RpcEndpoint, RpcEndpointRef, RpcEnv, RpcEnvServerConfig, RpcTimeout}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class Master(antConf: AntConfig) extends Logging {


}


object Master {

  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val config = RpcEnvServerConfig(new RpcConf(), "hello-server", host, 52345)
    val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
    val masterEndpoint: RpcEndpoint = new MasterEndpoint(rpcEnv)
    rpcEnv.setupEndpoint("master-service", masterEndpoint)
    rpcEnv.awaitTermination()
  }
}


class MasterEndpoint(override val rpcEnv: RpcEnv) extends RpcEndpoint with Logging {

  @volatile
  private var running = false
  private var activeMaster = false
  private val workerAddress = new ConcurrentHashMap[String, RpcEndpointRef]()
  private val workerLastHeartbeat = new ConcurrentHashMap[String, Long]()
  private val version = 0

  override def onStart(): Unit = {
    running = true
    // 去zk创建临时znode,
    //    创建成功,则表示当前节点是activeMaster, 并创建版本id(持久节点,当master切换时,检查版本id并自增,防止脑裂)
    //    创建失败,当前节点是standBy,监听active Master的临时节点
    //        临时节点消失,通知所有worker,master变更
    //        获取worker心跳信息
    logInfo(s"Ant master start .....")
  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    /**
     * worker:
     * 注册worker 信息:在zk上创建持久znode
     * worker 心跳信息
     *
     * Job元数据信息
     * Job运行信息
     *
     *
     * client:
     * worker信息
     * 注册Job信息
     *
     *
     *
     * standBy master:
     * isAlive?
     * killSelf?
     *
     *
     *
     */
    handleMsg(context)

  }

  override def receive: PartialFunction[Any, Unit] = {
    handleMsg(null)
  }

  override def onNetworkError(cause: Throwable, remoteAddress: RpcAddress): Unit = {

  }

  override def onStop(): Unit = {
    running = false
    stop()
  }

  def handleMsg(ctx: RpcCallContext): PartialFunction[Any, Unit] ={
    case RegisterWorker(address, port) =>
      val registered = workerAddress.keys().asScala
        .exists(ipAndPort => ipAndPort.equals(s"$address:$port"))
      if(!registered) {
        ctx.reply(Success)
      } else
        ctx.sendFailure(new ExistsException(s"worker:$address:$port has registered!"))

    // TODO: 心跳机制添加重试次数,可能某些情况导致节点假死
    case Heartbeat(address, port) =>
      val timeout = 120
      val currentTime = System.currentTimeMillis()
      val lastTime = workerLastHeartbeat.getOrDefault(s"$address:$port", -1)
      if(lastTime == -1) {
        // 可能存在之前心跳超时的情况,需要重新注册

      }
      if (currentTime - lastTime > timeout) {
        // 超时,剔除节点
        removeWorker(s"$address:$port")
        workerAddress.get(s"$address:$port").ask[StatusMsg](StopWorker)
        // todo:获取该节点的job信息,重新分配到其他存活的worker节点

      }

    case AddNewJob(cronExpression, runCommand, params, fileId, taskType) =>
      val taskParam = taskType match {
        case 0 =>
          // http 请求任务
          val heads = params.filter(t => t._1.startsWith("head")).asJava
          new HttpTask(params.getOrElse("method", "get"),
            params("url"), params.getOrElse("param", ""),
            heads, params.getOrElse("code", "true").toBoolean,
            params.getOrElse("timeout", "10").toLong)
        case 1 =>
          // 脚本任务
          new ScribeTask(runCommand)
        case 2 =>
          // soa-rpc 任务
          new SoaRpcTask()
        case _ =>
          ctx.sendFailure(new IllegalArgumentException("Unsupport task:only support http,scribe,soa-rpc!"))
          logError(s"task type:$taskType, now:${new Date()}")
          null
      }
      if (null != taskParam) {
        val taskId:Long = 0L // DB.insert(...)
        val info = new TaskInfo(taskId, cronExpression, taskParam)
        // 分配任务在哪个worker上执行
        val assignWorker = ""// DB.select(...)
        workerAddress.get(assignWorker).ask[ResponseMsg](info)
      }

    case DeleteJob(id) =>
      // 查询 job属于哪一个worker
      val ipAndPort:String = ""//DB
    val futureStatus = workerAddress.get(ipAndPort).ask[StatusMsg](DeleteJob(id))
      futureStatus.onComplete {
        case scala.util.Success(_) =>
          ctx.reply(Success)
        case scala.util.Failure(e) =>
          ctx.sendFailure(e)
      }
  }

  def removeWorker(ipAndPort: String): Unit ={
    workerAddress.remove(ipAndPort)
    workerLastHeartbeat.remove(ipAndPort)
  }
}

object MasterEndpoint {

}

class RequestMsg
case class Heartbeat(address:String, port:Int) extends RequestMsg
case class RegisterWorker(address:String, port:Int) extends RequestMsg
case class StopWorker() extends RequestMsg

case class AddNewJob(cronExpression: String, runCommand: String,
                     params:Map[String,String], fileId: Option[Long],
                     taskType: Int) extends RequestMsg
case class DeleteJob(id: Long) extends RequestMsg


case class ReportJob()
/* 状态信息（成功、失败） **/

class ResponseMsg
trait StatusMsg extends ResponseMsg
case class Success() extends StatusMsg
case class Fail() extends StatusMsg