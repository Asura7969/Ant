package com.github.ant.timer

import java.util.concurrent.atomic._
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.github.ant.job.TaskParam
import com.github.ant.network.protocol.message.TaskInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import scala.collection.mutable.ArrayBuffer

class TimerTest {

  private class TestTask(id: Int, latch: CountDownLatch,
                         output: ArrayBuffer[Int],
                         override var delayMs: Long,
                         override val crontabExpress: String) extends TimerTask {

    private[this] val completed = new AtomicBoolean(false)

    override val taskInfo: TaskInfo = new TaskInfo(id, crontabExpress, new TaskParam {
      override def doJob(): Unit = {
        if (completed.compareAndSet(false, true)) {
          output.synchronized { output += id }
          latch.countDown()
        }
      }

      override def getType: TaskParam.TaskType = TaskParam.TaskType.SCRIBE
    })
  }

  private[this] var timer: Timer = null

  @BeforeEach
  def setup(): Unit = {
    timer = new SystemTimer("test", tickMs = 1, wheelSize = 3)
  }

  @AfterEach
  def teardown(): Unit = {
    timer.shutdown()
  }

  @Test
  def testAlreadyExpiredTask(): Unit = {
    val output = new ArrayBuffer[Int]()

    val latches = (-5 until 0).map { i =>
      val latch = new CountDownLatch(1)
      timer.add(new TestTask( i, latch, output,i,""))
      latch
    }

    timer.advanceClock(0)

    latches.take(5).foreach { latch =>
      assertEquals(true, latch.await(3, TimeUnit.SECONDS), "already expired tasks should run immediately")
    }

    assertEquals(Set(-5, -4, -3, -2, -1), output.toSet, "output of already expired tasks")
  }

  @Test
  def testTaskExpiration(): Unit = {
    val output = new ArrayBuffer[Int]()

    val tasks = new ArrayBuffer[TestTask]()
    val ids = new ArrayBuffer[Int]()

    val latches =
      (0 until 5).map { i =>
        val latch = new CountDownLatch(1)
        tasks += new TestTask( i, latch, output,i,"")
        ids += i
        latch
      } ++ (10 until 100).map { i =>
        val latch = new CountDownLatch(2)
        tasks += new TestTask( i, latch, output,i,"")
        tasks += new TestTask( i, latch, output,i,"")
        ids += i
        ids += i
        latch
      } ++ (100 until 500).map { i =>
        val latch = new CountDownLatch(1)
        tasks += new TestTask( i, latch, output,i,"")
        ids += i
        latch
      }

    // randomly submit requests
    tasks.foreach { task => timer.add(task) }

    while (timer.advanceClock(2000)) {}

    latches.foreach { latch => latch.await() }

    assertEquals(ids.sorted, output.toSeq, "output should match")
  }
}
