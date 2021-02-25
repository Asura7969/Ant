package com.github.ant

import java.io.{File, FileInputStream}
import java.util.Properties

import com.github.ant.internal.Utils
import com.github.ant.rpc.RpcConf
import com.github.ant.utils.zk.CuratorUtils

import scala.collection.JavaConverters._

class GlobalConfig(env: Map[String, String]) {

  def loadFile: Properties = {
    val filePath = Utils.getDefaultPropertiesFile(env)
    val stream = new FileInputStream(new File(filePath))
    val properties = new Properties()
    properties.load(stream)
    properties
  }

  def toAntConfig: AntConfig = {
    val conf = new AntConfig()
    loadFile.asScala.foreach(kv => conf.set(kv._1,kv._2))
    conf
  }

  def toRpcConfig: RpcConf = {
    val conf = new RpcConf()
    loadFile.asScala.foreach(kv => if(kv._1.startsWith("ant.rpc.")) conf.set(kv._1,kv._2))
    conf
  }

  def toCuratorConfig: CuratorUtils.CuratorConfig = {
    val allConfig = loadFile
    new CuratorUtils.CuratorConfig()
      .setConnectAddr(allConfig.getProperty("ant.zookeeper.server"))
      .setConnectionTimeout(allConfig.getProperty("ant.zookeeper.timeout").toInt)
      .setMaxRetries(allConfig.getProperty("ant.zookeeper.max.retries").toInt)
      .setBaseSleepTimeMs(allConfig.getProperty("ant.zookeeper.sleep.ms").toInt)
      .setSessionTimeout(allConfig.getProperty("ant.zookeeper.session.timeout").toInt)
  }
}

object GlobalConfig {
  // 设置环境变量参考load-spark-env.sh
  // only for test
  val test_conf_path = "/Users/gongwenzhou/IdeaProjects/Ant/conf"
}
