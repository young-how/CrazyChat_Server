package com.dlut.crazychat.utils;


import com.alibaba.fastjson2.JSON;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@Component
public class RedisUtils {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 读取缓存 redis中key对应的值
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }


    /**
     * 写入String类型 到redis
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            //log.info("存入redis成功，key:{},value:{}",key, value);
            result = true;
        } catch (Exception e) {
            //log.info("存入redis失败，key:{},value:{}",key, value);
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 写入对象到redis      Json格式
     */
    public boolean setJsonString(final String key, Object value) {
        if (StringUtils.isBlank(key)) {
            //log.info("redis key值为空");
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, com.alibaba.fastjson2.JSON.toJSONString(value));
            //log.info("存入redis成功，key:{},value:{}",key, value);
            return true;
        } catch (Exception e) {
            //log.info("存入redis失败，key:{},value:{}",key, value);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新缓存
     */
    public boolean getAndSet(final String key, String value) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().getAndSet(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 删除缓存
     */
    public boolean delete(final String key) {
        boolean result = false;
        try {
            redisTemplate.delete(key);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 一个指定的 key 设置过期时间
     *
     * @param key
     * @param time
     */
    public boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key
     */
    public long getTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 移除指定key 的过期时间
     *
     * @param key
     */
    public boolean persist(String key) {
        return redisTemplate.boundValueOps(key).persist();
    }
}