/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ant.util

class AntException(message: String, cause: Throwable)
  extends Exception(message, cause) {

  def this(message: String) = this(message, null)
}

/**
 * Exception thrown when execution of some user code in the driver process fails, e.g.
 * accumulator update fails or failure in takeOrdered (user supplies an Ordering implementation
 * that can be misbehaving.
 */
private class AntDriverExecutionException(cause: Throwable)
  extends AntException("Execution error", cause)

/**
 * Exception thrown when the main user code is run as a child process (e.g. pyspark) and we want
 * the parent SparkSubmit process to exit with the same exit code.
 */
private case class AntUserAppException(exitCode: Int)
  extends AntException(s"User application exited with $exitCode")

/**
 * Exception thrown when the relative executor to access is dead.
 */
private case class ExecutorDeadException(message: String)
  extends AntException(message)

/**
 * Exception thrown when Spark returns different result after upgrading to a new version.
 */
private class AntUpgradeException(version: String, message: String, cause: Throwable)
  extends RuntimeException("You may get a different result due to the upgrading of Spark" +
    s" $version: $message", cause)
