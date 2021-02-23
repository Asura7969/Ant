package com.github.ant.db

import java.sql.Connection
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

import com.github.ant.AntConfig
import com.github.ant.internal.Logging

import collection.JavaConverters._

class DatabaseProvider(conf: AntConfig) extends Logging {
  protected val url: String = conf.get("ant.database.url")//"jdbc:mysql://localhost/mysql"
  protected val username: String = conf.get("ant.database.username", "root")
  protected val password: String = conf.get("ant.database.password", "root")

  protected val initCount: Int = conf.getInt("ant.database.initCount", 5)
  protected val minCount: Int = conf.getInt("ant.database.minCount", 3)
  protected val maxCount: Int = conf.getInt("ant.database.maxCount", 10)

  protected val pool = new ArrayBlockingQueue[Connection](maxCount)
  protected val currentCount = new AtomicLong(0)

  initPool()

  def initPool(): Unit = {
    throw new RuntimeException("")
  }

  def createConnection(): Connection = {
    throw new RuntimeException("")
  }

  def getConnection: Connection = {
    throw new RuntimeException("")
  }

  def releaseIdleCon(): Unit = {
    throw new RuntimeException("")
  }

  def returnConnection(con: Connection): Unit = {
    throw new RuntimeException("")
  }

  def close(): Unit = {
    pool.asScala.foreach(_.close())
  }

}

object DatabaseProvider {

  def build(conf: AntConfig): DatabaseProvider = {
    new DatabaseProvider(conf)
  }
}

