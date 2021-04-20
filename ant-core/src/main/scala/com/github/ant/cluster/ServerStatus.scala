package com.github.ant.cluster

/**
 * server status
 * <ul>
 *   <li>WAIT_SCHEDULE: 刚启动,待分配工作(master刚启动状态,等待zkServer通知)</li>
 *   <li>RUNNING: 正常运行状态</li>
 *   <li>STOPPED: 服务停止状态</li>
 *   <li>SCHEDULING: 主备切换状态(Master状态)</li>
 *   <li>FAILED: 服务异常状态</li>
 *   <li>UNKNOWN: 未知状态</li>
 * </ul>
 */
object SERVER_STATUS extends Enumeration {
  type SERVER_STATUS = Value

  val WAIT_SCHEDULE: SERVER_STATUS.Value = Value
  val RUNNING: SERVER_STATUS.Value = Value
  val STOPPED: SERVER_STATUS.Value = Value
  val SCHEDULING: SERVER_STATUS.Value = Value
  val FAILED: SERVER_STATUS.Value = Value
  val UNKNOWN: SERVER_STATUS.Value = Value
}
