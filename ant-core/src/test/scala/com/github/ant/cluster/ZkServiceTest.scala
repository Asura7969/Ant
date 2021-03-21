package com.github.ant.cluster

import com.github.ant.utils.zk.CuratorUtils
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener, PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.CreateMode
import org.junit.{After, Before, Test}

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author asura7969
 * @create 2021-03-20-17:24
 */
class ZkServiceTest {
  private var zkClient1:CuratorFramework = _
  private var zkClient2:CuratorFramework = _
  private val masterPath = "/ant-active-master"
  private val version = new AtomicInteger(0)

  val conf = new CuratorUtils.CuratorConfig()
    .setBaseSleepTimeMs(5000)
    .setConnectAddr("localhost:2181")
    .setMaxRetries(3)
    .setSessionTimeout(5000).build()

  @Test
  def zkServiceTest(): Unit = {
    val one = ZkService("ONE", conf)
    Thread.sleep(1000)
    val two = ZkService("TWO", conf)

    assert(one.getVersion() == 1)
    assert(two.getVersion() == 1)
    one.stop()
    Thread.sleep(1000)
    assert(two.getVersion() == 2)


  }




  def createClient(): CuratorFramework ={



    val retryPolicy = new ExponentialBackoffRetry(conf.getBaseSleepTimeMs, conf.getMaxRetries)
    val client = CuratorFrameworkFactory.builder.connectString(conf.getConnectAddr)
      .connectionTimeoutMs(conf.getConnectionTimeout).sessionTimeoutMs(conf.getSessionTimeout)
      .retryPolicy(retryPolicy).namespace("ant").build
    client.start()
    client
  }

  @Before
  def initClient(): Unit = {

    zkClient1 = createClient()
    zkClient2 = createClient()



  }

  @Test
  def createNode(): Unit = {

    new Thread(new Runnable {
      override def run(): Unit = {
        val stat = zkClient1.checkExists().forPath(masterPath)
        if (null == stat) {
          zkClient1.create.withMode(CreateMode.EPHEMERAL)
//            .forPath(masterPath, version.get().toString.getBytes("utf-8"))
            .forPath(masterPath, "0".getBytes())
          println("节点创建成功!")
        }

        println("获取节点数据!" + new String(zkClient1.getData.forPath(masterPath)))

      }
    },"thread-1").start()

    Thread.sleep(1000)
    new Thread(new Runnable {
      override def run(): Unit = {
        val stat = zkClient2.checkExists().forPath(masterPath)
        if (null != stat) {
          println("master 节点已经存在")
          println("thread-2 获取节点数据 : " + new String(zkClient2.getData.forPath(masterPath)))
          val nodeCache: NodeCache = new NodeCache(zkClient2, masterPath)
          nodeCache.start(true)
          nodeCache.getListenable.addListener(new NodeCacheListener {
            override def nodeChanged(): Unit = {
              println(nodeCache.getCurrentData)
              println(nodeCache.getPath)
            }
          })
          println("添加监听器成功！")
        } else {
          println("master 不存在")
        }

      }
    }, "thread-2").start()


    Thread.sleep(5000)



    zkClient1.close()

    Thread.sleep(100000)


  }

  @Test
  def threadListenerPathTest(): Unit ={
    new Thread(new Runnable {
      override def run(): Unit = {
        val stat = zkClient2.checkExists().forPath(masterPath)
        if (null != stat) {
          println("master 节点已经存在")
          println("thread-2 获取节点数据 : " + new String(zkClient2.getData.forPath(masterPath)))
          val nodeCache: NodeCache = new NodeCache(zkClient2, masterPath)
          nodeCache.start(true)
          nodeCache.getListenable.addListener(new NodeCacheListener {
            override def nodeChanged(): Unit = {
              println(nodeCache.getCurrentData)
              println(nodeCache.getPath)
            }
          })
          println("添加监听器成功！")
        } else {
          println("master 不存在")
        }

      }
    }, "thread-2").start()

    Thread.sleep(100000)
  }

  @After
  def stop(): Unit = {
    if (null != zkClient1) zkClient1.close()
    if (null != zkClient2) zkClient2.close()
  }


}
