package com.github.ant.timer

import com.github.ant.job.HttpTask
import com.github.ant.network.protocol.message.TaskInfo
import org.junit.jupiter.api.Test

class TimerServiceTest {
  val timer = new SystemTimer("test-worker", 1000, 60)
  val timeService = new TimeService(timer)

  @Test
  def addTaskTest(): Unit = {
    val task = new HttpTask("post", "http://localhost:9000", true, 1000)
    val info = new TaskInfo(1, "0 * * * * ?", task)
    timeService.addTask(info)
    assert(timeService.getTaskSize == 1, "添加任务不成功")

    Thread.sleep(1000 * 60 * 2)
  }

}
