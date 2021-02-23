package com.github.ant.db.mysql

import java.sql.{Connection, DriverManager}

import com.github.ant.AntConfig
import com.github.ant.db.DatabaseProvider

/**
 * mysql链接提供者
 * @param conf 数据库连接配置
 */
class MySqlProvider(conf: AntConfig) extends DatabaseProvider(conf) {

  import MySqlProvider._

  override def initPool(): Unit = {
    if (initCount == 0) {
      logError("mysql init count must more than zero!")
      throw new IllegalArgumentException("mysql init count must more than zero!")
    }
    (0 until initCount).foreach {
      _ =>
        val success = pool.add(createConnection())
        if (success) {
          currentCount.incrementAndGet()
        }
    }
  }

  override def createConnection(): Connection = {
    try {
      Class.forName(driver)
      DriverManager.getConnection(url, username, password)
    } catch {
      case ex:Exception =>
        throw new RuntimeException(s"create connection failed : ${ex.getMessage}")
    }
  }

  override def getConnection: Connection = {
    synchronized {
      if (!pool.isEmpty) {
        pool.poll()
      } else {
        if (currentCount.get() < maxCount) {
          currentCount.incrementAndGet()
          createConnection()
        } else {
          pool.take()
        }
      }
    }
  }

  override def releaseIdleCon(): Unit = {
    synchronized {
      if (currentCount.get() > minCount && pool.size() > 0) {
        pool.poll().close()
        currentCount.decrementAndGet()
      }
    }
  }

  override def returnConnection(con: Connection): Unit = {
    pool.add(con)
    releaseIdleCon()
  }


}

object MySqlProvider {
  private val driver = "com.mysql.jdbc.Driver"
}
