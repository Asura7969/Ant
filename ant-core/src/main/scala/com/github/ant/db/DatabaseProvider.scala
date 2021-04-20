package com.github.ant.db

import com.github.ant.AntConfig
import com.github.ant.internal.Logging
import com.github.ant.mybatis.mapper._
import com.github.ant.mybatis.model._
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.{SqlSession, SqlSessionFactory, SqlSessionFactoryBuilder}

import collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}

class DatabaseProvider(conf: AntConfig) extends Logging {
  val localSqlSession: ThreadLocal[SqlSession] = new ThreadLocal()
  lazy private val sqlSessionFactory: SqlSessionFactory = {
    val resource = "mybatis-config.xml"
    val inputStream = Resources.getResourceAsStream(resource)
    new SqlSessionFactoryBuilder().build(inputStream)
  }

  def getSqlSession: SqlSession = {
    sqlSessionFactory.openSession(true)
  }

  /**
   * 依据 主键id 获取单个结果
   * @param id 主键id
   * @return
   */
  def getOneById[T >: Null](id: Long)(implicit tag: ClassTag[T]): T = execute {
    val c = tag.runtimeClass
    val session = localSqlSession.get()
    val result = c match {
      case _ if c == classOf[Task] =>
        val mapper = session.getMapper[TaskMapper](classOf[TaskMapper])
        mapper.selectByPrimaryKey(id)

      case _ if c == classOf[TaskFile] =>
        val mapper = session.getMapper[TaskFileMapper](classOf[TaskFileMapper])
        mapper.selectByPrimaryKey(id)

      case _ if c == classOf[TaskParam]=>
        val mapper = session.getMapper[TaskParamMapper](classOf[TaskParamMapper])
        mapper.selectByPrimaryKey(id)

      case _ if c == classOf[TaskRunningInfo] =>
        val mapper = session.getMapper[TaskRunningInfoMapper](classOf[TaskRunningInfoMapper])
        mapper.selectByPrimaryKey(id)

      case _ if c == classOf[TaskRuntime] =>
        val mapper = session.getMapper[TaskRuntimeMapper](classOf[TaskRuntimeMapper])
        mapper.selectByPrimaryKey(id)

      case _ =>
        throw new UnsupportedOperationException(s"${c.getSimpleName} not found!")
    }
    if (null != result) c.cast(result).asInstanceOf[T] else null
  }

  /**
   * 获取各个节点的taskId 列表
   * @return
   */
  def groupByAddress(): Map[String, List[TaskRuntime]] = execute {
    val session = localSqlSession.get()
    val mapper = session.getMapper[TaskRuntimeMapper](classOf[TaskRuntimeMapper])
    val runtimes = mapper.selectAddressAndTaskId().asScala.toList
    runtimes.groupBy(_.getRuntimeAddress)
  }

  def insert[T](record: T)(implicit tag: ClassTag[T]): Long = execute {
    val session = localSqlSession.get()
    val c = tag.runtimeClass
    c match {
      case _ if c == classOf[Task] =>
        val mapper = session.getMapper[TaskMapper](classOf[TaskMapper])
        val task = record.asInstanceOf[Task]
        mapper.insertSelective(task)
        task.getTaskId

      case _ if c == classOf[TaskFile] =>
        val mapper = session.getMapper[TaskFileMapper](classOf[TaskFileMapper])
        val file = record.asInstanceOf[TaskFile]
        mapper.insertSelective(file)
        file.getFileId

      case _ if c == classOf[TaskParam]=>
        val mapper = session.getMapper[TaskParamMapper](classOf[TaskParamMapper])
        val param = record.asInstanceOf[TaskParam]
        mapper.insertSelective(param)
        param.getParamId

      case _ if c == classOf[TaskRunningInfo] =>
        val mapper = session.getMapper[TaskRunningInfoMapper](classOf[TaskRunningInfoMapper])
        val info = record.asInstanceOf[TaskRunningInfo]
        mapper.insertSelective(info)
        info.getTaskRunningInfoId

      case _ if c == classOf[TaskRuntime] =>
        val mapper = session.getMapper[TaskRuntimeMapper](classOf[TaskRuntimeMapper])
        val runtime = record.asInstanceOf[TaskRuntime]
        mapper.insertSelective(runtime)
        runtime.getRuntimeId

      case _ =>
        throw new UnsupportedOperationException(s"${tag.runtimeClass.getSimpleName} not found!")
    }
  }

  def update[T](record: T)(implicit tag: ClassTag[T]): Long = execute {
    val session = localSqlSession.get()
    val c = tag.runtimeClass
    c match {
      case _ if c == classOf[Task] =>
        val mapper = session.getMapper[TaskMapper](classOf[TaskMapper])
        val task = record.asInstanceOf[Task]
        mapper.updateByPrimaryKeySelective(task)
        task.getTaskId

      case _ if c == classOf[TaskFile] =>
        val mapper = session.getMapper[TaskFileMapper](classOf[TaskFileMapper])
        val file = record.asInstanceOf[TaskFile]
        mapper.updateByPrimaryKeySelective(file)
        file.getFileId

      case _ if c == classOf[TaskParam]=>
        val mapper = session.getMapper[TaskParamMapper](classOf[TaskParamMapper])
        val param = record.asInstanceOf[TaskParam]
        mapper.updateByPrimaryKeySelective(param)
        param.getParamId

      case _ if c == classOf[TaskRunningInfo] =>
        val mapper = session.getMapper[TaskRunningInfoMapper](classOf[TaskRunningInfoMapper])
        val info = record.asInstanceOf[TaskRunningInfo]
        mapper.updateByPrimaryKeySelective(info)
        info.getTaskRunningInfoId

      case _ if c == classOf[TaskRuntime] =>
        val mapper = session.getMapper[TaskRuntimeMapper](classOf[TaskRuntimeMapper])
        val runtime = record.asInstanceOf[TaskRuntime]
        mapper.updateByPrimaryKeySelective(runtime)
        runtime.getRuntimeId

      case _ =>
        throw new UnsupportedOperationException(s"${tag.runtimeClass.getSimpleName} not found!")
    }
  }

  def deleteById[T](id: Long)(implicit tag: ClassTag[T]): Int = execute {
    val c = tag.runtimeClass
    val session = localSqlSession.get()
    c match {
      case _ if c == classOf[Task] =>
        val mapper = session.getMapper[TaskMapper](classOf[TaskMapper])
        mapper.deleteByPrimaryKey(id)

      case _ if c == classOf[TaskFile] =>
        val mapper = session.getMapper[TaskFileMapper](classOf[TaskFileMapper])
        mapper.deleteByPrimaryKey(id)

      case _ if c == classOf[TaskParam]=>
        val mapper = session.getMapper[TaskParamMapper](classOf[TaskParamMapper])
        mapper.deleteByPrimaryKey(id)

      case _ if c == classOf[TaskRunningInfo] =>
        val mapper = session.getMapper[TaskRunningInfoMapper](classOf[TaskRunningInfoMapper])
        mapper.deleteByPrimaryKey(id)

      case _ if c == classOf[TaskRuntime] =>
        val mapper = session.getMapper[TaskRuntimeMapper](classOf[TaskRuntimeMapper])
        mapper.deleteByPrimaryKey(id)

      case _ =>
        throw new UnsupportedOperationException(s"${c.getSimpleName} not found!")
    }
  }

  /**
   * 依据主机地址获取任务列表id
   * @param address 主机地址
   * @return
   */
  def getTaskListByAddress(address: String): Seq[Task] = execute {
    val session = localSqlSession.get()
    val mapper = session.getMapper[TaskMapper](classOf[TaskMapper])
    mapper.getTaskListByAddress(address).asScala
  }


  private def execute[T](body: => T): T = synchronized {
    execute(getSqlSession)(body)
  }

  private def execute[T](session: SqlSession)(body: => T): T = synchronized {
    localSqlSession.set(session)
    try {
      body
    } catch {
      case e: Exception =>
        log.error("", e)
        throw e
    } finally {
      session.close()
      localSqlSession.remove()
    }
  }

}

object DatabaseProvider {

  def apply(conf: AntConfig): DatabaseProvider = new DatabaseProvider(conf)
}


