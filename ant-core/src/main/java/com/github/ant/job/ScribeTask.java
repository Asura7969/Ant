package com.github.ant.job;

import java.time.LocalDateTime;
import java.util.Objects;

public class ScribeTask extends TaskParam {

    private String command;

    @Override
    public void doJob() {
        System.out.println(LocalDateTime.now() + "脚本任务工作");
    }

    @Override
    public com.github.ant.job.TaskParam.TaskType getType() {
        return com.github.ant.job.TaskParam.TaskType.SCRIBE;
    }

    public ScribeTask(String command) {
        this.command = command;
    }

    public ScribeTask() {
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScribeTask that = (ScribeTask) o;
        return Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command);
    }
}
