package com.xunce.utils;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

public class RedisClusterAsync {

    private static Logger logger = LoggerFactory.getLogger(RedisClusterAsync.class);
    private static final RedisClusterAsync m_instance = new RedisClusterAsync();
    private RedisClusterClient m_cluster_client;
    private StatefulRedisClusterConnection<String, String> m_cluster_connect;
    private static final int expire_30_day_seconds = 2592000; // 30 * 24 * 60 * 60

    private RedisClusterAsync() {
        m_cluster_client = null;
        m_cluster_connect = null;
    }

    /**
     * @return RedisManager singleton instance
     */
    public final static RedisClusterAsync Instance() {
        return m_instance;
    }

    /**
     * Init RedisClusterClient
     *
     * @param hosts
     * @param ports
     * @return
     */
    public boolean Init(String hosts, String ports) {

        if (m_cluster_client == null) {
            String[] host = hosts.split(",");
            String[] port = ports.split(",");
            ArrayList<RedisURI> uris = new ArrayList<>();
            for (int i = 0; i <= host.length - 1; i++) {
                RedisURI node = RedisURI.create(host[i], Integer.parseInt(port[i]));
                uris.add(node);
            }
            m_cluster_client = RedisClusterClient.create(uris);
            ClusterTopologyRefreshOptions options = ClusterTopologyRefreshOptions.builder().enablePeriodicRefresh(Duration.ofMinutes(10)).enableAllAdaptiveRefreshTriggers().build();
            // set max queue size is 1e6
            m_cluster_client.setOptions(ClusterClientOptions.builder().autoReconnect(true).pingBeforeActivateConnection(true).topologyRefreshOptions(options).build());
            m_cluster_connect = m_cluster_client.connect();
        }
        return (m_cluster_connect != null);
    }

    public boolean isOpen() {
        if (m_cluster_connect != null) {
            return m_cluster_connect.isOpen();
        }
        return false;
    }

    /**
     * Closes all connections to host
     *
     * @param host Redis host description
     */
    public synchronized void Release(String host) {
        if (m_cluster_client != null) {
            m_cluster_client.shutdown();
        }
    }

    /**
     * @param skey
     * @param //这里不会返回异常，如果redis连接断开， 内部会不断尝试重连，并且没执行成功的redis命令会尝试再次执行
     * @return
     */
    public boolean hmset(String skey, Map<String, String> map) {
        if (m_cluster_connect == null) {
            return false;
        }

        RedisAdvancedClusterAsyncCommands<String, String> commands = m_cluster_connect.async();
        RedisFuture<String> future = commands.hmset(skey, map);
        future.thenAccept(result -> {
            if (!result.equals("OK")) {
                logger.error("hmset error: " + result);
            }
        });
        RedisFuture<Boolean> expire = commands.expire(skey, expire_30_day_seconds);
        expire.thenAccept(result -> {
        });
        return true;
    }
}