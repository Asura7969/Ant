package com.github.ant.timer

import java.util.concurrent.ConcurrentHashMap

import com.github.ant.internal.Logging
import com.github.ant.network.protocol.message.TaskInfo

import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * woker 端具体执行类,任务的注册,删除,获取所有
 */
class TimeService extends Logging{

  private val taskInfo = new ConcurrentHashMap[Long, TaskInfo]()

  def addTask(task: TaskInfo): Unit = {
    if (taskInfo.get(task.getTaskId) == null) {
      taskInfo.put(task.getTaskId, task)
    } else {
      logError(s"task already exists: ${task.getTaskId}")
      // todo：上报master
    }
  }

  def removeTask(task: TaskInfo): Unit = {
    taskInfo.remove(task.getTaskId)
    // todo: 上报master
  }

  def getAllTask: Seq[TaskInfo] = {
    taskInfo.values().asScala.toList
    // todo: 是否上报master

  }

  def getTask(taskId: Long): TaskInfo = {
    taskInfo.get(taskId)
    // todo: 是否上报master

  }

  def getTaskSize: Long = {
    taskInfo.size()
  }

  def checkAll(tasks: Seq[TaskInfo]): Seq[TaskInfo] = {
    val diffTask = new ArrayBuffer[TaskInfo]()
    tasks.foreach(task => {
      val info = Option(taskInfo.get(task.getTaskId))
      info match {
        case Some(t) =>
          if (!t.equals(task)) {
            diffTask.append(t)
            removeTask(t)
            // 重新注册task
            addTask(task)
          }
        case None =>
          addTask(task)
      }
    })

    diffTask

  }

}

object TimeService {

}
