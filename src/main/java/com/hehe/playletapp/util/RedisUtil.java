package com.hehe.playletapp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
//    @SuppressWarnings("rawtypes")
//    @Autowired
//    private RedisTemplate redisTemplate;

    public void setStr(String key, String value) {
        setStr(key, value, 3600L);
    }

    public void setStr(String key, String value, Long time) {
        stringRedisTemplate.opsForValue().set(key, value);
        if (time != null){
            stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
        }
    }

    public Object getKey(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delKey(String key) {
        stringRedisTemplate.delete(key);
    }
}
