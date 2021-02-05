package com.github.ant.timer

import com.github.ant.network.protocol.message.TaskInfo

trait TimerTask extends Runnable {
  var delayMs: Long
  val crontabExpress: String
  val taskInfo: TaskInfo

  private[this] var timerTaskEntry: TimerTaskEntry = null

  def cancel(): Unit = {
    synchronized {
      if (null == timerTaskEntry) timerTaskEntry.remove()
      timerTaskEntry = null
    }
  }

  private[timer] def setTimerTaskEntry(entry: TimerTaskEntry): Unit = {
    synchronized {
      // if this timerTask is already held by an existing timer task entry,
      // we will remove such an entry first.
      if (timerTaskEntry != null && timerTaskEntry != entry)
        timerTaskEntry.remove()

      timerTaskEntry = entry
    }
  }

  override def run(): Unit = {
    taskInfo.getTaskParam.doJob()
  }

  private[timer] def getTimerTaskEntry: TimerTaskEntry = timerTaskEntry
}


