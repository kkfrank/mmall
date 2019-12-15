package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool; //jedis连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total"));//最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle")); //最大空闲状态的jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle")); //最小空闲状态的jedis实例的个数
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow")); //在borrow一个jedis实例的时候，是否进行验证操作，如果true，则得到的jedis实例一定可用
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return")); //在return一个jedis实例的时候，是否进行验证操作，如果true，则return回jedispool的jedis实例一定可用

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        //连接耗尽的时候，是否阻塞，true的时候，阻塞等待，false的时候，阻塞抛错，默认是ttrue
        //config.setBlockWhenExhausted(true);

        pool = new JedisPool(config, redisIp, redisPort, 1000*2);
    }

    static {
        initPool();
    }

    //从连接池里拿jedis实例
    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        jedis.set("a", "fkkkkk");
        System.out.println(jedis.get("a"));
        returnResource(jedis);
        pool.destroy();//临时调用，销毁连接池中的所有连接

        try {
            Integer a= 1/0;
        }catch (Exception e){
            System.out.println("error");
        }
        System.out.println("me");
    }
}
