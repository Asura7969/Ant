package com.github.ant.job;

import java.time.LocalDateTime;

public class SoaRpcTask extends TaskParam {

    @Override
    public void doJob() {
        System.out.println(LocalDateTime.now() + "soa-rpc任务");
    }

    @Override
    public TaskType getType() {
        return TaskType.SOA_RPC;
    }
}
