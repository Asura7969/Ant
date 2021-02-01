package com.github.ant.utils.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorUtils {

    public static CuratorFramework createMaster(CuratorConfig conf) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(conf.getBaseSleepTimeMs(), conf.getMaxRetries());
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(conf.getConnectAddr()).connectionTimeoutMs(conf.getConnectionTimeout())
                .sessionTimeoutMs(conf.getSessionTimeout())
                .retryPolicy(retryPolicy)
                .namespace("super")
                .build();
        client.start();
        return client;
    }


    static class CuratorConfig {
        private String CONNECT_ADDR;
        private int SESSION_TIMEOUT;
        private int CONNECTION_TIMEOUT;
        private int MAX_RETRIES;
        private int BASE_SLEEP_TIME_MS;

        public CuratorConfig setConnectAddr(String connectAddr) {
            this.CONNECT_ADDR = connectAddr;
            return this;
        }

        public String getConnectAddr() {
            return this.CONNECT_ADDR;
        }

        public CuratorConfig setSessionTimeout(int sessionTimeout) {
            this.SESSION_TIMEOUT = sessionTimeout;
            return this;
        }

        public int getSessionTimeout() {
            return this.SESSION_TIMEOUT;
        }

        public CuratorConfig setConnectionTimeout(int connectionTimeout) {
            this.CONNECTION_TIMEOUT = connectionTimeout;
            return this;
        }

        public int getConnectionTimeout() {
            return this.CONNECTION_TIMEOUT;
        }

        public CuratorConfig setMaxRetries(int maxRetries) {
            this.MAX_RETRIES = maxRetries;
            return this;
        }

        public int getMaxRetries() {
            return this.MAX_RETRIES;
        }

        public CuratorConfig setBaseSleepTimeMs(int baseSleepTimeMs) {
            this.BASE_SLEEP_TIME_MS = baseSleepTimeMs;
            return this;
        }

        public int getBaseSleepTimeMs() {
            return this.BASE_SLEEP_TIME_MS;
        }

        public CuratorConfig build() {
            return this;
        }
    }
}
