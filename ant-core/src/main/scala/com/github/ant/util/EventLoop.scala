package com.github.ant.util

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{BlockingQueue, LinkedBlockingDeque}

import com.github.ant.internal.Logging

import scala.util.control.NonFatal

abstract class EventLoop[E](name:String) extends Logging{

  private val eventQueue: BlockingQueue[E] = new LinkedBlockingDeque[E]()
  private val stopped = new AtomicBoolean(false)

  private val eventThread = new Thread(name) {
    setDaemon(true)

    override def run(): Unit = {
      try {
        while (!stopped.get()) {
          val event = eventQueue.take()
          try {
            onReceive(event)
          } catch {
            case NonFatal(e) =>
              try {
                onError(e)
              } catch {
                case NonFatal(e) => logError("Unexpected error in " + name, e)
              }
          }
        }
      } catch {
        case ie: InterruptedException =>
        case NonFatal(e) => logError("Unexpected error in " + name, e)
      }
    }
  }

  def start(): Unit = {
    if (!stopped.get()) {
      throw new IllegalStateException(name + " has already been stopped")
    }
    onStart()
    eventThread.start()
  }

  def stop(): Unit = {
    if (stopped.compareAndSet(false, true)) {
      eventThread.interrupt()
      var onStopCalled = false
      try {
        eventThread.join()
        onStopCalled = true
        onStop()
      } catch {
        case ie: InterruptedException =>
          Thread.currentThread().interrupt()
          if (!onStopCalled) {
            // ie is thrown from `eventThread.join()`. Otherwise, we should not call `onStop` since
            // it's already called.
            onStop()
          }
      }
    } else {
      // do nothing, 该方法可以调用多次
    }
  }

  def post(event: E): Unit = {
    eventQueue.put(event)
  }

  def isActive: Boolean = eventThread.isAlive

  protected def onStart(): Unit = {}

  protected def onStop(): Unit = {}

  protected def onReceive(event: E): Unit = {}

  protected def onError(e: Throwable): Unit = {}


}
