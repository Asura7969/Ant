package com.github.ant

import java.util.concurrent.ConcurrentHashMap

import com.github.ant.function.NotFoundException
import com.github.ant.internal.Utils.getLocalAddress
import com.github.ant.internal.{Logging, Utils}
import com.github.ant.utils.zk.CuratorUtils

class AntConfig extends Logging with Serializable {
  private val settings = new ConcurrentHashMap[String, String]()

  def loadFromSystemProperties(): AntConfig = {
    // Load any ant.* system properties
    for ((key, value) <- Utils.getSystemProperties if key.startsWith("ant.")) {
      set(key, value)
    }
    this
  }

  def set(key: String, value: String): AntConfig = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key)
    }
    settings.put(key, value)
    this
  }

  def setAll(settings: Traversable[(String, String)]): AntConfig = {
    settings.foreach { case (k, v) => set(k, v) }
    this
  }

  def remove(key: String): AntConfig = {
    settings.remove(key)
    this
  }

  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  /**
   * @param key ant.cluster.worker.address
   *            ant.cluster.master.address
   *            ant.cluster.zkService.address
   * @return （address, port）
   */
  def getLocalServerInfo(key: String): (String, Int) = {
    get(key).split(",")
      .find(address => address.startsWith(s"$getLocalAddress:")) match {
      case Some(str) =>
        val addressStr = str.split(":")
        (addressStr(0), addressStr(1).toInt)
      case None =>
        throw new NotFoundException(s"Not found address like $getLocalAddress:%")
    }
  }

  def getLocalMasterUrl: String = {
    get("ant.cluster.master.address").split(",")
      .find(address => address.startsWith(s"$getLocalAddress:")) match {
      case Some(str) =>
        val addressStr = str.split(":")
        s"ant://${addressStr(0)}:${addressStr(1).toInt}"
      case None =>
        throw new NotFoundException(s"Not found address like $getLocalAddress:%")
    }
  }

  def getInt(key: String): Int = {
    getOption(key).getOrElse(throw new NoSuchElementException(key)).toInt
  }

  def getInt(key: String, defaultValue: Int): Int = {
    getOption(key).getOrElse(defaultValue).toString.toInt
  }

  def get(key: String, defaultValue: String): String = {
    getOption(key).getOrElse(defaultValue)
  }

  def getOption(key: String): Option[String] = {
    Option(settings.get(key))
  }

  def toCuratorConfig: CuratorUtils.CuratorConfig = {
    new CuratorUtils.CuratorConfig()
      .setConnectAddr(settings.get("ant.zookeeper.server"))
      .setConnectionTimeout(settings.get("ant.zookeeper.timeout").toInt)
      .setMaxRetries(settings.get("ant.zookeeper.max.retries").toInt)
      .setBaseSleepTimeMs(settings.get("ant.zookeeper.sleep.ms").toInt)
      .setSessionTimeout(settings.get("ant.zookeeper.session.timeout").toInt)
      .setLocalMasterUrl(getLocalMasterUrl)
  }
}
