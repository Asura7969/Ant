package com.github.ant.job;

import java.time.LocalDateTime;

public class SoaRpcTask extends TaskParam {

    @Override
    public void doJob() {
        System.out.println(LocalDateTime.now() + "soa-rpc任务");
    }

    @Override
    public com.github.ant.job.TaskParam.TaskType getType() {
        return com.github.ant.job.TaskParam.TaskType.SOA_RPC;
    }
}
