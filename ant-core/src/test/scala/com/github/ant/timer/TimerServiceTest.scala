package com.github.ant.timer

import java.util.Date
import java.util.concurrent.{Callable, CountDownLatch, Executors, Future, ThreadFactory, TimeUnit}

import com.github.ant.job.{HttpTask, TaskParam}
import com.github.ant.network.protocol.message.TaskInfo
import com.github.ant.utils.CronUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import scala.collection.mutable.ArrayBuffer

class TimerServiceTest {
  val timer = new SystemTimer("test-worker", 1000, 60)
  val timeService = new TimeService(timer)
  val task = new HttpTask("post", "http://localhost:80", true, 1000)
  val info = new TaskInfo(1, "0 * * * * ?", task)
  @Test
  def addTaskTest(): Unit = {
    timeService.addTask(info)
    assert(timeService.getTaskSize == 1, "添加任务不成功")

    Thread.sleep(1000 * 60 * 2)
  }

  private class CountDownTask(latch: CountDownLatch) extends TaskParam {
    override def getType: TaskParam.TaskType = TaskParam.TaskType.SCRIBE

    override def doJob(): Unit = {
      Thread.sleep(1000)
      latch.countDown()
    }
  }

  private class CallableTest(latch: CountDownLatch, timeout:Long) extends Callable[Boolean] {
    override def call(): Boolean = {
      latch.await(timeout, TimeUnit.MILLISECONDS)
    }
  }

  @Test
  def addMoreTask(): Unit = {
    val taskSize = 1000
    val cronExpression = "0 * * * * ?"
    val output = new ArrayBuffer[Future[Boolean]]()
    val service = Executors.newCachedThreadPool()

    val curDate = new Date()
    val executionTime = CronUtils.getNextExecuteTime(cronExpression, curDate)
    val timeout = executionTime.getTime - curDate.getTime + 1000 + 500
    (1 to taskSize).foreach { i=>
      val latch = new CountDownLatch(1)
      timeService.addTask(new TaskInfo(i, cronExpression, new CountDownTask(latch)))
      output.append(service.submit(new CallableTest(latch, timeout)))
    }

    output.foreach(r => {
      assertEquals(true, r.get(), "Task timed out")
    })

    println("main is done")
  }

  @Test
  def removeTask(): Unit = {
    timeService.addTask(info)
    assert(timeService.getTaskSize == 1, "添加任务不成功")
    timeService.removeTask(info.getTaskId)

    println("main is done")
  }


}
