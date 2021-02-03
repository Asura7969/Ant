package com.github.ant.message

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.github.ant.cluster.HttpTask
import io.netty.buffer.{ByteBuf, ByteBufAllocator}
import org.junit.jupiter.api.Test

class RpcMessage {

  @Test
  def encodeTest(): Unit ={
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//    import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
//    import com.fasterxml.jackson.annotation.PropertyAccessor
//    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
    val buf = ByteBufAllocator.DEFAULT.buffer()
    val httpTask = HttpTask("get", "http://localhost:8080", None, Option.apply(Map("head" -> "value")))
    println(mapper.writeValueAsString(httpTask))


//    httpTask.encode(buf)
//    val decodeResult = httpTask.decode(buf)
//    assert(decodeResult.isInstanceOf[HttpTask])
//    val task = decodeResult.asInstanceOf[HttpTask]
//    println(task)
//    assert(httpTask.equals(decodeResult))
  }



}
