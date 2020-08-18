package org.tinygame.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandle.CmdHandlerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis 使用工具
 */
public final class RedisUtil {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * Redis 连接池
     */
    private static JedisPool _jedisPool = null;

    /**
     * 私有化类默认构造器
     */
    private RedisUtil() {
    }

    /**
     * 初始化
     */
    public static void init (){
        try {
            _jedisPool = new JedisPool("127.0.0.1",6379);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    public static Jedis getRedis(){
        if (null == _jedisPool) {
            throw new RuntimeException("Jedis 还没有初始化");
        }

        Jedis redis = _jedisPool.getResource();
        redis.auth("");

        return redis;
    }
}
