package com.github.ant.util

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.github.ant.function.FatalExitError
import com.github.ant.internal.Logging

abstract class ShutdownableThread(val name: String,
                                  val isInterruptible: Boolean = true)
  extends Thread(name) with Logging{
  this.setDaemon(false)

  private val shutdownInitiated = new CountDownLatch(1)
  private val shutdownComplete = new CountDownLatch(1)
  @volatile private var isStarted: Boolean = false

  def shutdown(): Unit = {
    initiateShutdown()
    awaitShutdown()
  }

  def isShutdownInitiated: Boolean = shutdownInitiated.getCount == 0

  def isShutdownComplete: Boolean = shutdownComplete.getCount == 0

  def isThreadFailed: Boolean = isShutdownComplete && !isShutdownInitiated

  def initiateShutdown(): Boolean = {
    this.synchronized {
      if (isRunning) {
        logInfo("Shutting down")
        shutdownInitiated.countDown()
        if (isInterruptible)
          interrupt()
        true
      } else
        false
    }
  }

  def awaitShutdown(): Unit = {
    if (!isShutdownInitiated)
      throw new IllegalStateException("initiateShutdown() was not called before awaitShutdown()")
    else {
      if (isStarted)
        shutdownComplete.await()
      logInfo("Shutdown completed")
    }
  }

  def pause(timeout: Long, unit: TimeUnit): Unit = {
    if (shutdownInitiated.await(timeout, unit))
      logTrace("shutdownInitiated latch count reached zero. Shutdown called.")
  }

  def doWork(): Unit

  override def run(): Unit = {
    isStarted = true
    logInfo("Starting")
    try {
      while (isRunning)
        doWork()
    } catch {
      case e: FatalExitError =>
        shutdownInitiated.countDown()
        shutdownComplete.countDown()
        logInfo("Stopped")
//        Exit.exit(e.statusCode())
      case e: Throwable =>
        if (isRunning)
          logError("Error due to", e)
    } finally {
      shutdownComplete.countDown()
    }
    logInfo("Stopped")
  }

  def isRunning: Boolean = !isShutdownInitiated
}
