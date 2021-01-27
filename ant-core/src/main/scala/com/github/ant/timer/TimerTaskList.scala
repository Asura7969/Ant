package com.github.ant.timer

import java.util.concurrent.{Delayed, TimeUnit}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.github.ant.utils.Time

import scala.math.{Ordered, max}

private[timer] class TimerTaskList(taskCounter: AtomicInteger) extends Delayed{

  private[this] val root = new TimerTaskEntry(null, -1)
  root.next = root
  root.prev = root

  private[this] val expiration = new AtomicLong(-1L)

  def setExpiration(expirationMs: Long): Boolean = {
    expiration.getAndSet(expirationMs) != expirationMs
  }

  def getExpiration: Long = expiration.get

  // Apply the supplied function to each of tasks in this list
  def foreach(f: (TimerTask)=>Unit): Unit = {
    synchronized {
      var entry = root.next
      while (entry ne root) {
        val nextEntry = entry.next

        if (!entry.cancelled) f(entry.timerTask)

        entry = nextEntry
      }
    }
  }

  // Add a timer task entry to this list
  def add(timerTaskEntry: TimerTaskEntry): Unit = {
    var done = false
    while (!done) {
      // Remove the timer task entry if it is already in any other list
      // We do this outside of the sync block below to avoid deadlocking.
      // We may retry until timerTaskEntry.list becomes null.
      timerTaskEntry.remove()

      synchronized {
        timerTaskEntry.synchronized {
          if (timerTaskEntry.list == null) {
            // put the timer task entry to the end of the list. (root.prev points to the tail entry)
            val tail = root.prev
            timerTaskEntry.next = root
            timerTaskEntry.prev = tail
            timerTaskEntry.list = this
            tail.next = timerTaskEntry
            root.prev = timerTaskEntry
            taskCounter.incrementAndGet()
            done = true
          }
        }
      }
    }
  }

  def remove(timerTaskEntry: TimerTaskEntry): Unit = {
    synchronized {
      timerTaskEntry.synchronized {
        if (timerTaskEntry.list eq this) {
          timerTaskEntry.next.prev = timerTaskEntry.prev
          timerTaskEntry.prev.next = timerTaskEntry.next
          timerTaskEntry.next = null
          timerTaskEntry.prev = null
          timerTaskEntry.list = null
          taskCounter.decrementAndGet()
        }
      }
    }
  }

  // Remove all task entries and apply the supplied function to each of them
  def flush(f: TimerTaskEntry => Unit): Unit = {
    synchronized {
      var head = root.next
      while (head ne root) {
        remove(head)
        f(head)
        head = root.next
      }
      expiration.set(-1L)
    }
  }

  override def getDelay(unit: TimeUnit): Long = {
    unit.convert(max(getExpiration - Time.SYSTEM.hiResClockMs, 0), TimeUnit.MILLISECONDS)
  }

  override def compareTo(d: Delayed): Int = {
    val other = d.asInstanceOf[TimerTaskList]
    java.lang.Long.compare(getExpiration, other.getExpiration)
  }
}


private[timer] class TimerTaskEntry(val timerTask: TimerTask, val expirationMs: Long) extends Ordered[TimerTaskEntry] {

  @volatile
  var list: TimerTaskList = null
  var next: TimerTaskEntry = null
  var prev: TimerTaskEntry = null

  if (timerTask != null) timerTask.setTimerTaskEntry(this)

  def cancelled: Boolean = {
    timerTask.getTimerTaskEntry != this
  }

  def remove(): Unit = {
    var currentList = list
    while (null != currentList) {
      currentList.remove(this)
      currentList = list
    }
  }

  override def compare(that: TimerTaskEntry): Int = {
    java.lang.Long.compare(expirationMs, that.expirationMs)
  }
}