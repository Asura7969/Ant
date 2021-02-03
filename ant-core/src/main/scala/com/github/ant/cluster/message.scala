package com.github.ant.cluster

import com.github.ant.network.protocol.Encoders
import io.netty.buffer.ByteBuf
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

abstract class TaskParam extends Serializable {
  protected val mapper:ObjectMapper = new ObjectMapper() with ScalaObjectMapper
//  mapper.registerModule(DefaultScalaModule)
//  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  def encode(buf: ByteBuf): Unit
  def decode(buf: ByteBuf): TaskParam
}
// http 任务
case class HttpTask(getOrPost: String,
               url: String,
               param: Option[String],
               header: Option[Map[String, String]],
               successCode: Boolean = true,
               timeout: Long = 10) extends TaskParam {
  override def encode(buf: ByteBuf): Unit = {

    Encoders.Strings.encode(buf, mapper.writeValueAsString(this))
  }

  override def decode(buf: ByteBuf): TaskParam = {
    mapper.readValue(Encoders.Strings.decode(buf), classOf[HttpTask])
  }
}

// 脚本任务：python， shell， jar
class ScribtTask(command: String) extends TaskParam {
  override def encode(buf: ByteBuf): Unit = {
    Encoders.Strings.encode(buf, mapper.writeValueAsString(this))
  }

  override def decode(buf: ByteBuf): TaskParam = {
    mapper.readValue(Encoders.Strings.decode(buf), classOf[ScribtTask])
  }
}
// soa rpc任务
class SoaRpcTask() extends TaskParam {
  override def encode(buf: ByteBuf): Unit = {
    Encoders.Strings.encode(buf, mapper.writeValueAsString(this))
  }

  override def decode(buf: ByteBuf): TaskParam = {
    mapper.readValue(Encoders.Strings.decode(buf), classOf[SoaRpcTask])
  }
}

// 任务分发 请求/响应
case class Task(taskId: Long, cronExpression: String, task: TaskParam)
case class AssignTask(task: Seq[Task], time: Long = System.currentTimeMillis())
case class ReceiveTaskResponse(status: Boolean)

// work启动后 向master注册
case class RegisterWorker(hostName: String)
// Master 返回 workId
case class RegisterWorkerResponse(workId: Long)


// 节点通信
case class Heartbeat(workId: Long, hostName: String, time: Long = System.currentTimeMillis())
case class LiveRequest()
case class LiveResponse(isLive: Boolean = true)
case class ReportTasks()
case class TaskInfoResponse(workId: Long, tasks: Seq[Task])
case class WorkRestart(workId: Long)
case class WorkStop(workId: Long)
case class ChangeMaster()