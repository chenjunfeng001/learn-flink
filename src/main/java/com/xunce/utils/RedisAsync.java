package com.xunce.utils;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class RedisAsync {

    private static Logger logger = LoggerFactory.getLogger(RedisAsync.class);
    private static final RedisAsync m_instance = new RedisAsync();
    private RedisClient m_client;
    private StatefulRedisConnection<String, String> m_connect;
    // 30 * 24 * 60 * 60
    private static final int expire_30_day_seconds = 2592000;

    private RedisAsync() {
        m_client = null;
        m_connect = null;
    }

    /**
     * @return RedisManager singleton instance
     */
    public final static RedisAsync Instance() {
        return m_instance;
    }

    /**
     * @param redis_conn_str "redis://192.168.0.152:6379/0"
     * @return
     */
    public boolean Init(String redis_conn_str) {
        if (m_client == null) {
            m_client = RedisClient.create(redis_conn_str);
            // set max queue size is 1e6
            m_client.setOptions(ClientOptions.builder().autoReconnect(true).requestQueueSize(1000000).build());
            m_connect = m_client.connect();
        }
        return (m_connect != null);
    }

    public boolean isOpen() {
        if (m_connect != null) {
            return m_connect.isOpen();
        }
        return false;
    }

    /**
     * Closes all connections to host
     *
     * @param host Redis host description
     */
    public synchronized void Release(String host) {
        if (m_client != null) {
            m_client.shutdown();
        }
    }

    /**
     * @param skey
     * @param //这里不会返回异常，如果redis连接断开， 内部会不断尝试重连，并且没执行成功的redis命令会尝试再次执行
     * @return
     */
    public boolean hmset(String skey, Map<String, String> map) {
        if (m_connect == null) {
            return false;
        }
        RedisAsyncCommands<String, String> commands = m_connect.async();
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