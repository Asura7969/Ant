package com.github.ant.cluster

import java.net.InetAddress

import com.github.ant.internal.Logging
import com.github.ant.utils.zk.CuratorUtils
import com.github.ant.utils.zk.CuratorUtils.{addNodeCache, createClient, createEphemeral, getData, isNodeExistSync}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCacheListener
import java.util.concurrent.atomic.AtomicInteger

import com.github.ant.GlobalConfig
import com.github.ant.cluster.master.{BecameActive, BecameStandBy, RequestMsg, StatusMsg, Success}
import com.github.ant.rpc.netty.NettyRpcEnvFactory
import com.github.ant.rpc.{RpcAddress, RpcCallContext, RpcEndpoint, RpcEndpointRef, RpcEnv, RpcEnvServerConfig}

/**
 * @author asura7969
 * @create 2021-03-20-16:59
 */
class ZkServiceEndpoint(conf: CuratorUtils.CuratorConfig, override val rpcEnv: RpcEnv)
  extends RpcEndpoint with Logging {

  private val masterPath = "/ant-active-master"
  private var zkClient:CuratorFramework = _
  @volatile private var running = false
  /* true:active, false:standBy */
  @volatile private var activeOrStandBy = false
  private val version = new AtomicInteger(0)

  var localMaster: RpcEndpointRef = rpcEnv.setupEndpointRef(
    RpcAddress("localhost", 52345), "master-service")

  override def onStart(): Unit = {
    try {
      zkClient = createClient(conf)
      createOrWatchPath()
      running = true
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
            log.info(s"node has changed, ${rpcEnv.address.hostPort} is Ant-Master")
            activeOrStandBy = true
            // todo:成为 active-master 后的后续工作
            sendToLocalMaster(BecameActive())
          }
        }
      })
      sendToLocalMaster(BecameStandBy())
    } else {
      // 创建并成为master节点
      createEphemeral(zkClient, masterPath, version.incrementAndGet().toString.getBytes)
      log.info(s"${rpcEnv.address.hostPort} is Ant-Master")
      activeOrStandBy = true
      // todo:成为 active-master 后的后续工作
      sendToLocalMaster(BecameActive())
    }

  }

  def sendToLocalMaster(msg: RequestMsg): Unit = {
    localMaster.askSync[StatusMsg](msg) match {
      case Success() => log.info(s"${rpcEnv.address.hostPort} has becomed active Ant-Master")
      case _ =>
        val errorLog = s"${rpcEnv.address.hostPort} maybe has down"
        log.error(errorLog)
        throw new RuntimeException(errorLog)
    }
  }

  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    handleMsg(context)
  }

  override def receive: PartialFunction[Any, Unit] = {
    handleMsg(null)
  }

  def handleMsg(ctx: RpcCallContext): PartialFunction[Any, Unit] = {
    // 询问master是否还存活

    return null
  }

  override def onStop(): Unit = {
    super.onStop()
    if (null != zkClient) {
      zkClient.close()
    }
    if (running) {
      running = false
    }
    log.info("ZkService has closed!")
    stop()
  }

  def getVersion: Int = {
    version.get()
  }

  def getActiveOrStandByFlag: Boolean = {
    activeOrStandBy
  }
}

object ZkServiceEndpoint {

  def apply(name:String, conf: CuratorUtils.CuratorConfig): ZkServiceEndpoint = new ZkServiceEndpoint(name, conf)

  def main(args: Array[String]): Unit = {
    val globalConfig = new GlobalConfig(Map("ANT_CONF_DIR" -> System.getenv("ANT_CONF_DIR")))
    val conf = globalConfig.toAntConfig
    val masterHosts = conf.get("ant.cluster.master.host").split(",")
    val localhost = InetAddress.getLocalHost.getHostAddress
    val masterHost = masterHosts.find(host => host.startsWith(s"$localhost:"))
    val zkServicePort = conf.get("ant.cluster.zkService.port").toInt
    val rpcConfig = globalConfig.toRpcConfig
    val config = RpcEnvServerConfig(rpcConfig, s"$localhost:$zkServicePort", localhost, zkServicePort)
    val rpcEnv: RpcEnv = NettyRpcEnvFactory.create(config)
    val zkServiceEndpoint: RpcEndpoint = new ZkServiceEndpoint(conf.toCuratorConfig, rpcEnv)
    rpcEnv.setupEndpoint(rpcEnv.address.hostPort, zkServiceEndpoint)
    rpcEnv.awaitTermination()
  }
}
