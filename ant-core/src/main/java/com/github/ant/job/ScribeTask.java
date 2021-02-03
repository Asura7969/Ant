package com.github.ant.job;

import java.util.Objects;

public class ScribeTask extends TaskParam {

    private String command;

    @Override
    public TaskType getType() {
        return TaskType.SCRIBE;
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
