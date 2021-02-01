package com.github.ant.rpc

import java.util.concurrent.TimeoutException

import com.github.ant.internal.Utils
import com.github.ant.util.ThreadUtils

import scala.concurrent.Future
import scala.concurrent.duration.{DurationLong, FiniteDuration}

/**
 * An exception thrown if RpcTimeout modifies a `TimeoutException`.
 */
private[rpc] class RpcTimeoutException(message: String, cause: TimeoutException)
  extends TimeoutException(message) { initCause(cause) }


/**
 * Associates a timeout with a description so that a when a TimeoutException occurs, additional
 * context about the timeout can be amended to the exception message.
 *
 * @param duration timeout duration in seconds
 * @param timeoutProp the configuration property that controls this timeout
 */
class RpcTimeout(val duration: FiniteDuration, val timeoutProp: String)
  extends Serializable {

  /** Amends the standard message of TimeoutException to include the description */
  private def createRpcTimeoutException(te: TimeoutException): RpcTimeoutException = {
    new RpcTimeoutException(te.getMessage + ". This timeout is controlled by " + timeoutProp, te)
  }

  /**
   * PartialFunction to match a TimeoutException and add the timeout description to the message
   *
   * @note This can be used in the recover callback of a Future to add to a TimeoutException
   * Example:
   *    val timeout = new RpcTimeout(5.milliseconds, "short timeout")
   *    Future(throw new TimeoutException).recover(timeout.addMessageIfTimeout)
   */
  def addMessageIfTimeout[T]: PartialFunction[Throwable, T] = {
    // The exception has already been converted to a RpcTimeoutException so just raise it
    case rte: RpcTimeoutException => throw rte
    // Any other TimeoutException get converted to a RpcTimeoutException with modified message
    case te: TimeoutException => throw createRpcTimeoutException(te)
  }

  /**
   * Wait for the completed result and return it. If the result is not available within this
   * timeout, throw a [[RpcTimeoutException]] to indicate which configuration controls the timeout.
   *
   * @param  future  the `Future` to be awaited
   * @throws RpcTimeoutException if after waiting for the specified time `future`
   *         is still not ready
   */
  def awaitResult[T](future: Future[T]): T = {
    try {
      ThreadUtils.awaitResult(future, duration)
    } catch addMessageIfTimeout
  }
}


object RpcTimeout {

  /**
   * Lookup the timeout property in the configuration and create
   * a RpcTimeout with the property key in the description.
   *
   * @param conf configuration properties containing the timeout
   * @param timeoutProp property key for the timeout in seconds
   * @throws NoSuchElementException if property is not set
   */
  def apply(conf: RpcConf, timeoutProp: String): RpcTimeout = {
    val timeout = { conf.getTimeAsSeconds(timeoutProp).seconds }
    new RpcTimeout(timeout, timeoutProp)
  }

  /**
   * Lookup the timeout property in the configuration and create
   * a RpcTimeout with the property key in the description.
   * Uses the given default value if property is not set
   *
   * @param conf configuration properties containing the timeout
   * @param timeoutProp property key for the timeout in seconds
   * @param defaultValue default timeout value in seconds if property not found
   */
  def apply(conf: RpcConf, timeoutProp: String, defaultValue: String): RpcTimeout = {
    val timeout = { conf.getTimeAsSeconds(timeoutProp, defaultValue).seconds }
    new RpcTimeout(timeout, timeoutProp)
  }

  /**
   * Lookup prioritized list of timeout properties in the configuration
   * and create a RpcTimeout with the first set property key in the
   * description.
   * Uses the given default value if property is not set
   *
   * @param conf configuration properties containing the timeout
   * @param timeoutPropList prioritized list of property keys for the timeout in seconds
   * @param defaultValue default timeout value in seconds if no properties found
   */
  def apply(conf: RpcConf, timeoutPropList: Seq[String], defaultValue: String): RpcTimeout = {
    require(timeoutPropList.nonEmpty)

    // Find the first set property or use the default value with the first property
    val itr = timeoutPropList.iterator
    var foundProp: Option[(String, String)] = None
    while (itr.hasNext && foundProp.isEmpty) {
      val propKey = itr.next()
      conf.getOption(propKey).foreach { prop => foundProp = Some((propKey, prop)) }
    }
    val finalProp = foundProp.getOrElse((timeoutPropList.head, defaultValue))
    val timeout = { Utils.timeStringAsSeconds(finalProp._2).seconds }
    new RpcTimeout(timeout, finalProp._1)
  }
}
