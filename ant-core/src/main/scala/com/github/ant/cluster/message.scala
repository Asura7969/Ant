package com.github.ant.cluster

trait TaskParam
// http 任务
class HttpTask(getOrPost: String,
               url: String, param: String,
               header: Map[String, String],
               successCode: Boolean = true,
               timeout: Long = 10) extends TaskParam
// 脚本任务：python， shell， jar
class ScribtTask(command: String) extends TaskParam
// soa rpc任务
class SoaRpcTask() extends TaskParam

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