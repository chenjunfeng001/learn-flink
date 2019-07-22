package com.xunce.utils;


import com.xunce.conf.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;

/**
 * @author junfeng.chen@xuncetech.com
 * @date 2019/5/25
 */
public class RedisPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);
    /**
     * jedis连接池
     */
    private static JedisPool pool;
    private static Integer maxTotal = ConfigurationManager.getInteger("redis.maxTotal");
    private static Integer maxIdle = ConfigurationManager.getInteger("redis.maxIdle");
    private static Integer minIdle = ConfigurationManager.getInteger("redis.minIdle");

    /**
     * 在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的
     * 在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的
     */
    private static Boolean testOnBorrow = ConfigurationManager.getBoolean("redis.testOnBorrow");
    private static Boolean testOnReturn = ConfigurationManager.getBoolean("redis.testOnReturn");

    private static String redisIp = ConfigurationManager.getProperty("redis.ip");
    private static Integer redisPort = ConfigurationManager.getInteger("redis.port");

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        // 连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
        config.setBlockWhenExhausted(true);
        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void returnResource(Jedis jedis) {
//		pool.returnResource(jedis);
        if (jedis != null) {
            pool.close();

        }
    }

    public static Long hset(int dbIndex, String key, String field, String value) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPoolUtil.getJedis();
            jedis.select(dbIndex);
            result = jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error("hset key:{} error", key, field, e);
            RedisPoolUtil.returnBrokenResource(jedis);
            return result;
        }
        RedisPoolUtil.returnResource(jedis);
        return result;
    }

    public static Map<String, String> hgetAll(int dbIndex, String key) {
        Jedis jedis = null;
        Map<String, String> maps = null;
        try {
            jedis = RedisPoolUtil.getJedis();
            jedis.select(dbIndex);
            maps = jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error("hgetAll key:{} error", key, e);
            RedisPoolUtil.returnBrokenResource(jedis);
            return maps;
        }
        RedisPoolUtil.returnResource(jedis);
        return maps;
    }

    public static Map<byte[], byte[]> hgetAll1(int dbIndex, String key) {
        Jedis jedis = null;
        Map<byte[], byte[]> maps = null;
        try {
            jedis = RedisPoolUtil.getJedis();
            jedis.select(dbIndex);
            maps = jedis.hgetAll(key.getBytes());
        } catch (Exception e) {
            logger.error("hgetAll key:{} error", key, e);
            RedisPoolUtil.returnBrokenResource(jedis);
            return maps;
        }
        RedisPoolUtil.returnResource(jedis);
        return maps;
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();


        Pipeline pipeline = jedis.pipelined();
        pipeline.select(1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            pipeline.set("p" + i, "p" + i);
        }
        List<Object> results = pipeline.syncAndReturnAll();
        long end = System.currentTimeMillis();
        System.out.println("Pipelined SET: " + ((end - start) / 1000.0) + " seconds");
        jedis.disconnect();
    }

}