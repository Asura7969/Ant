package com.github.ant.db

import java.util.Date

import com.github.ant.AntFunSuite
import com.github.ant.mybatis.model.{Task, TaskParam, TaskRuntime}

class DatabaseProviderTest
  extends AntFunSuite{

  private val db:DatabaseProvider = DatabaseProvider(null)

  test("insert、delete、get") {
    val paramId = db.insert[TaskParam](new TaskParam(new Date()))
    val tid = db.insert[Task](new Task("task1", new Date(), paramId, null, "task-command"))
    assert(db.getOneById[Task](tid).getTaskId == tid)
    db.deleteById[Task](tid)
    assert(db.getOneById[Task](tid) == null)
  }


  test("groupByAddress") {
    val record = new TaskRuntime(1L, "127.0.0.1")
    val id = db.insert[TaskRuntime](record)
    record.setRuntimeId(id)
    val map: Map[String, List[TaskRuntime]] = db.groupByAddress()
    assert(map.size == 1)
    assert(map("127.0.0.1").contains(record))
    db.deleteById[TaskRuntime](id)
  }


}
