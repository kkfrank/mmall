package com.mmall.util;

import com.mmall.common.RedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);

    public static String setEx(String key, int seconds, String value){
        Jedis jedis = null;
        String result;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key,seconds, value);
        } catch (Exception e){
            logger.error("setEx key:{} value:{} seconds:{} error", key, value, seconds, e);
            RedisPool.returnBrokenResource(jedis);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static Long expire(String key, int seconds){
        Jedis jedis = null;
        Long result;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, seconds);
        } catch (Exception e){
            logger.error("expire key:{} seconds:{} error", key, seconds, e);
            RedisPool.returnBrokenResource(jedis);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String set(String key, String value){
        Jedis jedis = null;
        String result;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e){
            logger.error("set key:{} value:{} error", key, value, e);
            RedisPool.returnBrokenResource(jedis);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        Jedis jedis = null;
        String result;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e){
            logger.error("get key:{} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key){
        Jedis jedis = null;
        Long result;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e){
            logger.error("del key:{} error", key, e);
            RedisPool.returnBrokenResource(jedis);
            return null;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();

        RedisPoolUtil.set("keyTest", "value");
        String value = RedisPoolUtil.get("keyTest");

        RedisPoolUtil.setEx("keyex",60*10, "valueex");

        RedisPoolUtil.expire("keyTest",60 *20);

        RedisPoolUtil.del("keyTest");
    }
}
