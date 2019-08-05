package com.xunce.utils;

import com.xunce.conf.ConfigurationManager;
import com.xunce.constants.Constants;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.*;

/**
 * jedis操作
 *
 * @author junfeng.chen@xuncetech.com
 * @date 2019/7/1
 */
public class RedisPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);

    private RedisPoolUtil() {
    }

    /**
     * jedis连接池
     */
    private static volatile JedisSentinelPool sentinelPool;
    private static volatile JedisPool jedisPool;

    private static String mode = ConfigurationManager.getProperty(Constants.REDIS_SERVER_MODE);

    private static Integer maxTotal = Integer.valueOf(ConfigurationManager.getProperty("redis.maxTotal"));
    private static Integer maxIdle = Integer.valueOf(ConfigurationManager.getProperty("redis.maxIdle"));
    private static Integer minIdle = Integer.valueOf(ConfigurationManager.getProperty("redis.minIdle"));

    /**
     * 在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的
     * 在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的
     */
    private static Boolean testOnBorrow = Boolean.valueOf(ConfigurationManager.getProperty("redis.testOnBorrow"));
    private static Boolean testOnReturn = Boolean.valueOf(ConfigurationManager.getProperty("redis.testOnReturn"));


    private static String password = ConfigurationManager.getProperty("redis.password");
    private static Integer timeout = Integer.valueOf(ConfigurationManager.getProperty("redis.timeout"));


    private static JedisSentinelPool getSentinelPool() {
        if (sentinelPool == null) {
            synchronized (RedisPoolUtil.class) {
                if (sentinelPool == null) {
                    String master = ConfigurationManager.getProperty("redis.mymaster");
                    String sentinels = ConfigurationManager.getProperty("redis.sentinels");
                    String splitPattern = ConfigurationManager.getProperty("redis.split.pattern");
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(maxTotal);
                    config.setMaxIdle(maxIdle);
                    config.setMinIdle(minIdle);
                    config.setTestOnBorrow(testOnBorrow);
                    config.setTestOnReturn(testOnReturn);
                    config.setMaxWaitMillis(180000);
                    // 连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
                    config.setBlockWhenExhausted(true);

                    Set<String> nodes = new HashSet<>(Arrays.asList(sentinels.split(splitPattern)));
                    sentinelPool = new JedisSentinelPool(master, nodes, config, timeout, password);
                }
            }
        }
        return sentinelPool;
    }

    private static JedisPool getJedisPool(){
        if(jedisPool == null){
            synchronized (RedisPoolUtil.class){
                if(jedisPool == null){
                    String ip = ConfigurationManager.getProperty("redis.ip");
                    Integer port = Integer.parseInt(ConfigurationManager.getProperty("redis.port"));

                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(maxTotal);
                    config.setMaxIdle(maxIdle);
                    config.setMinIdle(minIdle);

                    config.setTestOnBorrow(testOnBorrow);
                    config.setTestOnReturn(testOnReturn);
                    // 连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。
                    config.setBlockWhenExhausted(true);
                    jedisPool = new JedisPool(config, ip, port, 1000 * 2);
                }
            }
        }
        return jedisPool;
    }

    private static Jedis getJedis(int index) {
        Jedis jedis = null;
        switch (mode){
            case Constants.STANDALONE:
                jedis = getJedisPool().getResource();
                break;
            case Constants.SENTINEL:
                jedis = getSentinelPool().getResource();
                break;

            case Constants.CLUSTER:

                break;

            default:
                break;
        }
        jedis.select(index);
        return jedis;
    }


    // 对外提供方法
    public static String setKV(int index, String key, String value) {
        Jedis jedis = null;
        String set = null;
        try {
            jedis = getJedis(index);
            set = jedis.set(key, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }

        return set;
    }

    public static String getByK(int index, String key) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = getJedis(index);
            result = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
        return result;
    }

    public static Long hset(int dbIndex, String key, String field, String value) {
        Jedis jedis = null;
        Long hset = null;
        try {
            jedis = getJedis(dbIndex);
            hset = jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
        return hset;
    }

    public static Map<String, String> hgetAll(int dbIndex, String key) {
        Jedis jedis = null;
        Map<String, String> map = null;
        try {
            jedis = getJedis(dbIndex);
            map = jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }

        return map;
    }

    public static List<Map<String, String>> hgetAll(int dbIndex, String[] keys) {
        Jedis jedis = null;
        List<Map<String, String>> lists = Lists.newArrayList();
        try {
            jedis = getJedis(dbIndex);
            for (String key : keys) {
                Map<String, String> map = jedis.hgetAll(key);
                lists.add(map);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }

        return lists;

    }
    public static List<Map<String, String>> multiHgetAll(int index, String[] keys) {
        Jedis jedis = null;
        List<Map<String, String>> lists = Lists.newArrayList();
        try {
            jedis = RedisPoolUtil.getJedis(index);
            // 开启事务
            Transaction multi = jedis.multi();

            for (String key : keys) {
                multi.hgetAll(key);
            }
            // 提交事务
            List<Object> exec = multi.exec();
            for (Object obj : exec) {
                Map<String, String> map = (Map<String, String>) obj;
                lists.add(map);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
        return lists;
    }

    public static Map<String,Map<String,String>> hgetAllMap(int dbIndex, String[] keys) {
        Jedis jedis = null;
        Map<String,Map<String,String>> result = new HashMap<String,Map<String,String>>(500);
        Map<String,Response<Map<String,String>>> responses = new HashMap<String,Response<Map<String,String>>>(keys.length);
        try {
            jedis = getJedis(dbIndex);
            Pipeline pipelined = jedis.pipelined();
            for (String key : keys) {
                responses.put(key,pipelined.hgetAll(key));
            }
            pipelined.sync();
            responses.forEach((k,v) ->{
                result.put(k,v.get());
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }

        return result;

    }
    
    /**
     * 清空指定key对应数据
     */
    public static void delByKey(int index,String... keys){
        Jedis jedis =null;
        try {
            jedis = RedisPoolUtil.getJedis(index);
            jedis.del(keys);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                // 在JedisPool模式下，Jedis会被归还给资源池
                jedis.close();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        System.out.println(RedisPoolUtil.setKV(2, "jedis", "hello"));
    }
}
