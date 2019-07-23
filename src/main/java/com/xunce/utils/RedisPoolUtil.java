package com.xunce.utils;

import com.xunce.conf.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
    private static JedisPool jedisPool;
    private static Integer maxTotal = Integer.valueOf(ConfigurationManager.getProperty("redis.maxTotal"));
    private static Integer maxIdle = Integer.valueOf(ConfigurationManager.getProperty("redis.maxIdle"));
    private static Integer minIdle = Integer.valueOf(ConfigurationManager.getProperty("redis.minIdle"));

    /**
     * 在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的
     * 在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的
     */
    private static Boolean testOnBorrow = Boolean.valueOf(ConfigurationManager.getProperty("redis.testOnBorrow"));
    private static Boolean testOnReturn = Boolean.valueOf(ConfigurationManager.getProperty("redis.testOnReturn"));

    private static String redisIp = ConfigurationManager.getProperty("redis.ip");
    private static Integer redisPort = Integer.valueOf(ConfigurationManager.getProperty("redis.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        // 连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
        config.setBlockWhenExhausted(true);
        jedisPool = new JedisPool(config, redisIp, redisPort, 1000 * 2);

    }

    static {
        initPool();
    }

    private static Jedis getJedis() {
        return jedisPool.getResource();
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

        }finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
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
        }finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
        return maps;
    }


    public static void main(String[] args)  {

    }



}