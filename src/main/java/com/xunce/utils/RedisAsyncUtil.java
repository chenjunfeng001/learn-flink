package com.xunce.utils;

import com.alibaba.fastjson.JSONObject;
import com.xunce.conf.ConfigurationManager;
import com.xunce.constants.Constants;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 异步方式操作redis
 */
public class RedisAsyncUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisAsyncUtil.class);

    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    private static String mode = ConfigurationManager.getProperty(Constants.REDIS_SERVER_MODE);
    private static String splitPattern = ConfigurationManager.getProperty(Constants.REDIS_SPLIT_PATTERN);
    private static String password = ConfigurationManager.getProperty(Constants.REDIS_PASSWORD);
    private static Integer timeout = Integer.valueOf(ConfigurationManager.getProperty(Constants.REDIS_TIMEOUT));

    private RedisAsyncUtil() {

    }

    static {
        switch (mode){
            case Constants.STANDALONE:
                String host = ConfigurationManager.getProperty(Constants.REDIS_HOST);
                Integer port = Integer.parseInt(ConfigurationManager.getProperty(Constants.REDIS_PORT));
                RedisClient client = RedisClient.create(RedisURI.create(host, port));

                client.setOptions(ClientOptions.builder().autoReconnect(true).requestQueueSize(1000000).build());
                pool = ConnectionPoolSupport.createGenericObjectPool(client::connect, new GenericObjectPoolConfig());

                break;
            case Constants.SENTINEL:
                String sentinels = ConfigurationManager.getProperty(Constants.REDIS_SENTINELS);
                String master = ConfigurationManager.getProperty(Constants.REDIS_MYMASTER);

                List<String> sentinelsList = Arrays.asList(sentinels.split(splitPattern));

                RedisURI.Builder builder = RedisURI.builder().withSentinelMasterId(master).withTimeout(Duration.ofSeconds(timeout)).withPassword(password);

                for (String hostPort : sentinelsList) {
                    try {
                        String[] split = hostPort.split(":");
                        // 配置哨兵节点
                        builder.withSentinel(split[0], Integer.parseInt(split[1]));
                    } catch (Exception e) {
                        logger.error("redis.sentinels 属性配置错误！");
                    }
                }
                RedisClient redisClient = RedisClient.create(builder.build());
                // set max queue size is 1e6
                redisClient.setOptions(ClientOptions.builder().autoReconnect(true).requestQueueSize(1000000).build());
                pool = ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, new GenericObjectPoolConfig());
                break;

            case Constants.CLUSTER:

                break;

            default:
                break;
        }

    }

    private static StatefulRedisConnection<String, String> getStatefulRedisConnection() {
        try {
            return pool.borrowObject();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public static void release(StatefulRedisConnection<String, String> connection) {
        release(connection, null);
    }

    public static void release(StatefulRedisConnection<String, String> connection, RedisClient client) {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }


    public static List<Map<String, String>> hgetAllMap(String[] keys) {
        List<Map<String, String>> lists = Lists.newArrayList();
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.setAutoFlushCommands(true);
        for (String key : keys) {
            try {
                RedisFuture<Map<String, String>> hgetall = asyncCommands.hgetall(key);
                lists.add(hgetall.get(timeout, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
        release(connection);
        return lists;
    }

    /**
     * 异步 Pipelining 方式获取hash
     */
    public static List<Map<String, String>> hgetAllMapPipeline(String[] keys) {

        List<Map<String, String>> lists = Lists.newArrayList();
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        asyncCommands.setAutoFlushCommands(false);


        List<RedisFuture<Map<String, String>>> futures = Lists.newArrayList();

        for (String key : keys) {
            RedisFuture<Map<String, String>> hgetall = asyncCommands.hgetall(key);
            futures.add(hgetall);
        }

        asyncCommands.flushCommands();

        for (RedisFuture future : futures) {
            try {
                lists.add((Map<String, String>) future.get());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        release(connection);
        return lists;
    }

    /**
     * 异步 Pipelining 方式获取hash 的 value
     */
    public static List<List<String>> hgetAllValPipeline(String[] keys) {

        List<List<String>> lists = Lists.newArrayList();
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        asyncCommands.setAutoFlushCommands(false);

        List<RedisFuture<List<String>>> futures = Lists.newArrayList();

        for (String key : keys) {
            RedisFuture<List<String>> hvals = asyncCommands.hvals(key);
            futures.add(hvals);
        }

        asyncCommands.flushCommands();

        for (RedisFuture future : futures) {
            try {
                lists.add((List<String>)future.get());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        release(connection);
        return lists;
    }

    public static List<Map<String, String>> hscanAll(String[] keys) {
        List<Map<String, String>> lists = Lists.newArrayList();
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        for (String key : keys) {
            HashMap<String, String> resultMap = new HashMap<>();
            try {
                String cursor = "0";
                ScanCursor scanCursor = new ScanCursor(cursor, false);
                ScanArgs scanArgs = new ScanArgs().match("*").limit(100);

                do {
                    scanCursor.setCursor(cursor);
                    RedisFuture<MapScanCursor<String, String>> hscan = asyncCommands.hscan(key, scanCursor, scanArgs);
                    MapScanCursor<String, String> mapScanCursor = hscan.get(timeout, TimeUnit.MILLISECONDS);
                    cursor = mapScanCursor.getCursor();
                    Map<String, String> tmpMap = mapScanCursor.getMap();
                    resultMap.putAll(tmpMap);

                } while (!StringUtils.equals(cursor, "0"));

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            lists.add(resultMap);

        }
        release(connection);
        return lists;
    }


    public static List<List<String>> multiHgetAllVal(String[] keys) {
        List<List<String>> lists = Lists.newArrayList();

        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> async = connection.async();
        async.setAutoFlushCommands(false);
        try {
            // 开启事务
            async.multi();
            for (String key : keys) {
                async.hvals(key);
            }
            // 提交事务
            RedisFuture<TransactionResult> resultRedisFuture = async.exec();
            async.flushCommands();
            TransactionResult objects = resultRedisFuture.get();
            for (Object object : objects) {
                lists.add((List<String>) object);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release(connection);
        }
        return lists;
    }

    public static List<Map<String, String>> multiHgetAllMap(String[] keys) {
        List<Map<String, String>> lists = Lists.newArrayList();

        Map<String, Map<String, String>> result = new HashMap<>(500);
        Map<String, RedisFuture<Map<String, String>>> responses = new HashMap<>(keys.length);
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> async = connection.async();
        async.setAutoFlushCommands(false);
        try {
            // 开启事务
            async.multi();
            for (String key : keys) {
                async.hgetall(key);
            }
            // 提交事务
            RedisFuture<TransactionResult> resultRedisFuture = async.exec();
            async.flushCommands();
            TransactionResult objects = resultRedisFuture.get();
            for (Object object : objects) {
                Map<String, String> map = (Map<String, String>) object;
                lists.add(map);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release(connection);
        }
        return lists;
    }

    /**
     *
     */
    public static void set(String key, String value) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        RedisFuture<String> future = asyncCommands.set(key, value);
        release(connection);
    }

    public static void hset(String key, String field, String value) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(field, value);
        RedisFuture<String> future = asyncCommands.hmset(key, hashMap);
        release(connection);
    }

    public static void multiHset(String key, String field, String value) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> async = connection.async();

        // 开启事务操作
        RedisFuture<String> multi = async.multi();
        async.hset(key, field, value);
        async.exec();

        release(connection);
    }

    public static String hget(String key, String field) {
        String result = "";
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        RedisFuture<String> hget = asyncCommands.hget(key, field);
        try {
            result = hget.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release(connection);
        }
        return result;
    }

    /**
     *
     */
    public static void publish(String channel, String message) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.publish(channel, message);
        release(connection);
    }

    /**
     * 清空指定key对应数据
     */
    public void delByKey(String... keys) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        try {
            asyncCommands.del(keys);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release(connection);
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        String[] baseSourceKeys = {"tstockinfo","tasset","tcombi","tsecuritiesbranch","tfuturedepositratio","toptiondepositset","tfundinfo","tstockinfo", "tbondproperty", "tcurrencyrate","tfundstock", "tfundasset"};
        String[] baseSourceKeys = {"tstockinfo","tasset","tcombi","tsecuritiesbranch"};
        for (int i = 0;i< 100; i++){
            RedisAsyncUtil.set("flink"+i,"hello"+ new Random().nextInt(100));
        }

    }
}