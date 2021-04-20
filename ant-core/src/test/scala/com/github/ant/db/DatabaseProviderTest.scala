package com.github.ant.db

import java.util.Date

import com.github.ant.AntFunSuite
import com.github.ant.mybatis.model.{Task, TaskParam}

class DatabaseProviderTest
  extends AntFunSuite{

  private val db:DatabaseProvider = DatabaseProvider(null)

  test("insert、delete、get") {
    val paramId = db.insert[TaskParam](new TaskParam(new Date()))
    val tid = db.insert[Task](new Task("task1", new Date(), paramId, null, "task1-command"))
    assert(db.getOneById[Task](tid).getTaskId == tid)
    db.deleteById[Task](tid)
    assert(db.getOneById[Task](tid) == null)
  }


  test("groupByAddress") {

  }


}
