package com.mmall.util;

import com.mmall.common.RedisSharededPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;

public class RedisSharededPoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisSharededPoolUtil.class);

    public static String setEx(String key, int seconds, String value){
        ShardedJedis jedis = null;
        String result;

        try {
            jedis = RedisSharededPool.getJedis();
            result = jedis.setex(key, seconds, value);
        } catch (Exception e){
            logger.error("setEx key:{} value:{} seconds:{} error", key, value, seconds, e);
            RedisSharededPool.returnBrokenResource(jedis);
            return null;
        }
        RedisSharededPool.returnResource(jedis);
        return result;
    }

    public static Long expire(String key, int seconds){
        ShardedJedis jedis = null;
        Long result;

        try {
            jedis = RedisSharededPool.getJedis();
            result = jedis.expire(key, seconds);
        } catch (Exception e){
            logger.error("expire key:{} seconds:{} error", key, seconds, e);
            RedisSharededPool.returnBrokenResource(jedis);
            return null;
        }
        RedisSharededPool.returnResource(jedis);
        return result;
    }

    public static String set(String key, String value){
        ShardedJedis jedis = null;
        String result;

        try {
            jedis = RedisSharededPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e){
            logger.error("set key:{} value:{} error", key, value, e);
            RedisSharededPool.returnBrokenResource(jedis);
            return null;
        }
        RedisSharededPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis jedis = null;
        String result;

        try {
            jedis = RedisSharededPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e){
            logger.error("get key:{} error", key, e);
            RedisSharededPool.returnBrokenResource(jedis);
            return null;
        }
        RedisSharededPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result;

        try {
            jedis = RedisSharededPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e){
            logger.error("del key:{} error", key, e);
            RedisSharededPool.returnBrokenResource(jedis);
            return null;
        }
        RedisSharededPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        ShardedJedis jedis = RedisSharededPool.getJedis();

        RedisSharededPoolUtil.set("keyTest", "value");
        String value = RedisSharededPoolUtil.get("keyTest");

        RedisSharededPoolUtil.setEx("keyex",60*10, "valueex");

        RedisSharededPoolUtil.expire("keyTest",60 *20);

        RedisSharededPoolUtil.del("keyTest");
    }
}
