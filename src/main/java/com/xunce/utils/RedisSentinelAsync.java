package com.xunce.utils;

import com.xunce.conf.ConfigurationManager;
import com.xunce.constants.Constants;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 异步方式操作redis哨兵
 */
public class RedisSentinelAsync {

    private static Logger logger = LoggerFactory.getLogger(RedisSentinelAsync.class);

    private static RedisClient redisClient;
    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;
    private static String master = ConfigurationManager.getProperty(Constants.REDIS_MYMASTER);
    private static String sentinels = ConfigurationManager.getProperty(Constants.REDIS_SENTINELS);
    private static String splitPattern = ConfigurationManager.getProperty(Constants.REDIS_SPLIT_PATTERN);
    private static String password = ConfigurationManager.getProperty(Constants.REDIS_PASSWORD);
    private static Integer timeout = Integer.valueOf(ConfigurationManager.getProperty(Constants.REDIS_TIMEOUT));

    private RedisSentinelAsync() {

    }

    static {
        List<String> sentinelsList = Arrays.asList(sentinels.split(splitPattern));
        RedisURI.Builder builder = RedisURI.builder().withSentinelMasterId(master).withTimeout(Duration.ofSeconds(timeout)).withPassword(password);

        for (String hostPort : sentinelsList) {
            try {
                String[] split = hostPort.split(":");
                // 配置哨兵节点
                builder.withSentinel(split[0], Integer.valueOf(split[1]));
            } catch (Exception e) {
                logger.error("redis.sentinels 属性配置错误！");
            }
        }
        redisClient = RedisClient.create(builder.build());
        // set max queue size is 1e6
        redisClient.setOptions(ClientOptions.builder().autoReconnect(true).requestQueueSize(1000000).build());
        pool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(), new GenericObjectPoolConfig());

    }

    private static StatefulRedisConnection<String, String> getStatefulRedisConnection(RedisClient redisClient) {
        try {
            return pool.borrowObject();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public static void release(StatefulRedisConnection<String, String> connection) {
        release(connection,null);
    }

    public static void release(StatefulRedisConnection<String, String> connection, RedisClient client) {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }


    public static List<Map<String, String>> hgetAllMap(int dbIndex, String[] keys) {
        List<Map<String, String>> lists = Lists.newArrayList();
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.select(dbIndex);
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


    public static List<Map<String, String>> multiHgetAllMap(int index, String[] keys) {
        List<Map<String, String>> lists = Lists.newArrayList();

        Map<String, Map<String, String>> result = new HashMap<>(500);
        Map<String, RedisFuture<Map<String, String>>> responses = new HashMap<>(keys.length);
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> async = connection.async();
        async.select(index);
        try {
            // 开启事务
            async.multi();
            for (String key : keys) {
                async.hgetall(key);
            }
            // 提交事务
            RedisFuture<TransactionResult> resultRedisFuture = async.exec();
            TransactionResult objects = resultRedisFuture.get(timeout, TimeUnit.MILLISECONDS);
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
    public static void set(int index, String key, String value) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.select(index);
        RedisFuture<String> future = asyncCommands.set(key, value);
        release(connection);
    }

    public static void hset(int index, String key, String field, String value) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.select(index);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(field, value);
        RedisFuture<String> future = asyncCommands.hmset(key, hashMap);
        release(connection);
    }

    public static void multiHset(int index, String key, String field, String value){
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> async = connection.async();
        async.select(index);

        // 开启事务操作
        RedisFuture<String> multi = async.multi();
        async.hset(key,field,value);
        async.exec();

        release(connection);
    }

    public static String hget(int index, String key ,String field){
        String result = "";
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.select(index);

        RedisFuture<String> hget = asyncCommands.hget(key, field);
        try {
            result = hget.get(timeout, TimeUnit.MILLISECONDS);
        }  catch (Exception e) {
            logger.error(e.getMessage(), e);
        }finally {
            release(connection);
        }
        return result;
    }

    /**
     *
     */
    public static void publish(int index, String channel, String message) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        asyncCommands.select(index);
        asyncCommands.publish(channel, message);
        release(connection);
    }

    /**
     * 清空指定key对应数据
     */
    public void delByKey(int index, String... keys) {
        StatefulRedisConnection<String, String> connection = getStatefulRedisConnection(redisClient);
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        try {
            asyncCommands.select(index);
            asyncCommands.del(keys);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release(connection);
        }
    }

    public static void saveDatasetByHash(String key, String field, String value) {
        RedisSentinelAsync.hset(0, key, field, value);
    }


    public static void main(String[] args) throws InterruptedException {

        System.out.println(args[0]);

        String thgregister = RedisSentinelAsync.hget(0, "thgregister", "20090109,59058");
        System.out.println(thgregister);
//        int i= 0;
//        while (true){
//            i++;
//            System.out.println("i："+ i);
//            Thread.sleep(1000);
//            long start = System.currentTimeMillis();
//            StatefulRedisConnection<String, String> statefulRedisConnection = RedisSentinelAsync.getStatefulRedisConnection(redisClient);
//            long end = System.currentTimeMillis();
//            System.out.println(end - start);
//            RedisSentinelAsync.release(statefulRedisConnection,null);
//        }

//        RedisSentinelAsync.set(2,"hello2","redis2");


        // 准备查询Keys
//            String[] multiKeys = {"tfundstock", "tholdingdetail", "tinstructionstock", "tfundasset", "thgregister"};
//        List<Map<String, String>> maps = RedisSentinelAsync.multiHgetAllMap(0, multiKeys);

//        while (true) {
//            Thread.sleep(2000);
//
//            // 准备查询Keys
//            long start = System.currentTimeMillis();
//
//            String[] baseSourceKeys = {"tasset", "tcombi", "tsecuritiesbranch", "tfuturedepositratio", "toptiondepositset", "tfundinfo"};
//            // 准备查询Keys
//            String[] marketInfoSourceKeys = {"toptionproperty", "tstockinfo", "tfuturesinfo", "tbondproperty", "tcurrencyrate"};
//
//            // 准备查询Keys
//            String[] multiKeys = {"tfundstock", "tholdingdetail", "tinstructionstock", "tfundasset", "thgregister"};
//
//
//            List<Map<String, String>> hgetAll = RedisSentinelPoolUtil.hgetAll(0, baseSourceKeys);
//
//            List<Map<String, String>> marketInfoSource = RedisSentinelPoolUtil.hgetAll(0, marketInfoSourceKeys);
//
//            long end = System.currentTimeMillis();
//            System.out.println("耗时：" + (end - start));
//
//
//            List<Map<String, String>> maps = RedisSentinelAsync.hgetAllMap(0, baseSourceKeys);
//            System.out.println(maps.size());
//
//
//            List<Map<String, String>> maps1 = RedisSentinelAsync.hgetAllMap(0, marketInfoSourceKeys);
//            System.out.println(maps1.size());
//            System.out.println("lettuce异步耗时：" + (System.currentTimeMillis() - end));
//
//            System.out.println("-------------------------------");
//        }


    }
}