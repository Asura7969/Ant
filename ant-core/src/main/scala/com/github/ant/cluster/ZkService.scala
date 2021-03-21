package com.github.ant.cluster

import com.github.ant.internal.Logging
import com.github.ant.utils.zk.CuratorUtils
import com.github.ant.utils.zk.CuratorUtils.{addNodeCache, createClient, createEphemeral, getData, isNodeExistSync}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCacheListener

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author asura7969
 * @create 2021-03-20-16:59
 */
class ZkService(name: String, conf: CuratorUtils.CuratorConfig) extends Logging {

  private val masterPath = "/ant-active-master"
  private var zkClient:CuratorFramework = _
  @volatile private var running = false
  private val version = new AtomicInteger(0)

  start()
  private def start(): Unit = {
    try {
      zkClient = createClient(conf)
      running = true
      createOrWatchPath()
      log.info("ZkService created successfully!")
    } catch {
      case e:Exception =>
        stop()
        log.info("ZkService created failed!")
        throw e;
    }
  }

  /**
   * master选举，成功则成为active node;不成功，standby node 并监听路径
   */
  def createOrWatchPath(): Unit = {
    if (isNodeExistSync(zkClient, masterPath)) {
      // 设置version，保持version递增
      val currentVersion = getData(zkClient, masterPath).toInt
      version.set(currentVersion)
      // 已经存在active-master，添加监听器
      addNodeCache(zkClient, masterPath, new NodeCacheListener {
        override def nodeChanged(): Unit = {
          if (!isNodeExistSync(zkClient, masterPath)) {
            createEphemeral(zkClient, masterPath, version.incrementAndGet().toString.getBytes)
            log.info(s"node has changed, $name is Ant-Master")
            // todo:成为 active-master 后的后续工作
          }
        }
      })
    } else {
      // 创建并成为master节点
      createEphemeral(zkClient, masterPath, version.incrementAndGet().toString.getBytes)
      log.info(s"$name is Ant-Master")
      // todo:成为 active-master 后的后续工作
    }

  }

  def stop(): Unit = {
    if (null != zkClient) {
      zkClient.close()
    }
    if (running) {
      running = false
    }
    log.info("ZkService has closed!")
  }

  def getVersion(): Int = {
    version.get()
  }

}

object ZkService {

  def apply(name:String, conf: CuratorUtils.CuratorConfig): ZkService = new ZkService(name, conf)

}
