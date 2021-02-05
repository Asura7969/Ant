package com.github.ant.timer

import java.util.concurrent.ConcurrentHashMap

import com.github.ant.internal.Logging
import com.github.ant.network.protocol.message.TaskInfo
import com.github.ant.util.ShutdownableThread
import com.github.ant.utils.CronUtils._

import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

/**
 * worker 端具体执行类,任务的注册,删除,获取所有
 */
class TimeService(timer: Timer) extends Logging{
  import TimeService._
  private val timeAdvancer = new TimeAdvancer()
  timeAdvancer.start()
  private val taskInfo = new ConcurrentHashMap[Long, TaskInfo]()

  private class TimeAdvancer extends ShutdownableThread(
    name = "worker-advancer", isInterruptible = false) {

    override def doWork(): Unit = {
      timer.advanceClock(WorkTimeoutMs)
    }
  }

  def shutdown(): Unit = {
    timeAdvancer.shutdown()
  }

  def addTask(task: TaskInfo): Unit = {
    if (taskInfo.get(task.getTaskId) == null) {
      taskInfo.put(task.getTaskId, task)
      Try{
        handleJob(task)
      } match {
        case Success(value) => timer.add(value)
        case Failure(ex) =>
          println("有异常信息,需要上报master处理")
          // TODO: 上报 master 添加任务失败,需包含本节点信息与任务信息和异常信息
      }
    } else {
      logError(s"task already exists: ${task.getTaskId}")
      // todo：上报master
    }
  }

  def removeTask(taskId: Long): Unit = {
    taskInfo.remove(taskId)
    timer.remove(taskId)
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
            removeTask(t.getTaskId)
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

object TimeService extends Logging {
  private val WorkTimeoutMs: Long = 200L

  def handleJob(task: TaskInfo): TimerTask = {
    val nextTime = getNextExecuteTime(task.getCronExpression).getTime
    logInfo(s"taskId:${task.getTaskId} next execution time:$nextTime")
    val delay = nextTime - System.currentTimeMillis()
    new TimerTask {
      override var delayMs: Long = delay
      override val crontabExpress: String = task.getCronExpression
      override val taskInfo: TaskInfo = task
    }
  }
}
