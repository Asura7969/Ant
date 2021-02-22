package com.github.ant

import java.util.concurrent.ConcurrentHashMap

import com.github.ant.internal.{Logging, Utils}

class AntConfig extends Logging with Serializable {
  private val settings = new ConcurrentHashMap[String, String]()

  private[spark] def loadFromSystemProperties(): AntConfig = {
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
}
