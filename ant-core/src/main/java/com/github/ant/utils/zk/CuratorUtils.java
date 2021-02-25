package com.github.ant.utils.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;

public class CuratorUtils {

    public static CuratorFramework createClient(CuratorConfig conf) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(conf.getBaseSleepTimeMs(), conf.getMaxRetries());
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(conf.getConnectAddr()).connectionTimeoutMs(conf.getConnectionTimeout())
                .sessionTimeoutMs(conf.getSessionTimeout())
                .retryPolicy(retryPolicy)
                .namespace("ant")
                .build();
        client.start();
        return client;
    }

    public static void createExpiredNode(final CuratorFramework client, final String path, final byte[] data) {

    }

    public static void watchPath(final CuratorFramework client, final String path, final byte[] data) throws Exception {
        client.getData().usingWatcher(new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeDeleted:
                    case NodeDataChanged:
                    case DataWatchRemoved:


                }
            }
        }).forPath(path);

    }

    public static void create(final CuratorFramework client, final String path, final byte[] payload) throws Exception {
        client.create().creatingParentsIfNeeded().forPath(path, payload);
    }

    public static void createEphemeral(final CuratorFramework client, final String path, final byte[] payload) throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public static String createEphemeralSequential(final CuratorFramework client, final String path, final byte[] payload) throws Exception {
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public static void setData(final CuratorFramework client, final String path, final byte[] payload) throws Exception {
        client.setData().forPath(path, payload);
    }

    public static void delete(final CuratorFramework client, final String path) throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }

    public static void guaranteedDelete(final CuratorFramework client, final String path) throws Exception {
        client.delete().guaranteed().forPath(path);
    }

    public static String getData(final CuratorFramework client, final String path) throws Exception {
        return new String(client.getData().forPath(path));
    }

    public static List<String> getChildren(final CuratorFramework client, final String path) throws Exception {
        return client.getChildren().forPath(path);
    }



    public static class CuratorConfig {
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
