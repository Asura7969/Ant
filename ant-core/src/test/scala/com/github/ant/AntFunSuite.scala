package com.github.ant

import com.github.ant.internal.Logging
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite}

class AntFunSuite extends FunSuite
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Logging{

  override protected def beforeAll(): Unit = {

  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }
}
