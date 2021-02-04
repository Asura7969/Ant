package com.github.ant.timer

trait TimerTask extends Runnable {
  var delayMs: Long
  val crontabExpress: String

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

  private[timer] def getTimerTaskEntry: TimerTaskEntry = timerTaskEntry
}


