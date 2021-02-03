package com.github.ant.cluster

import com.github.ant.job.TaskParam



// 脚本任务：python， shell， jar

// soa rpc任务


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