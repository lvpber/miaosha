package com.lvpb.miaosha.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lvpb.miaosha.model.redis.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisOperator
{
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Gson gson = new GsonBuilder().create();

    /**======================================常用方法============================================*/
    //设置对象
    public <T> boolean set(KeyPrefix prefix, String key, T value)
    {
        /** 生成真正的key */
        String realKey = prefix.getPrefix() + key;

        int seconds = prefix.expireSeconds();
        String jsonStr = gson.toJson(value);

        if(seconds <= 0)
        {
            stringRedisTemplate.opsForValue().set(realKey,jsonStr);
        }
        else
        {
            stringRedisTemplate.opsForValue().set(realKey,jsonStr,seconds,TimeUnit.SECONDS);
        }
        return true;
    }

    //获取对象
    public <T> T get(KeyPrefix prefix,String key,Class<T> clazz)
    {
        /** 生成真正的key */
        String realKey = prefix.getPrefix() + key;

        String jsonStr = stringRedisTemplate.opsForValue().get(realKey);
        if(jsonStr == null)
            return null;
        return gson.fromJson(jsonStr,clazz);
    }

    //判断对象是否存在
    public boolean exists(KeyPrefix prefix,String key)
    {
        String realKey = prefix.getPrefix() + key;
        return stringRedisTemplate.hasKey(realKey);
    }

    /**
     * 原子操作增加key
     * @param prefix 前缀
     * @param key    逻辑key
     * @param delta  增量
     * @return       增加后的结果
     */
    public long incr(KeyPrefix prefix,String key,long delta)
    {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().increment(realKey,delta);
    }

    /**
     * 原子操作减少key
     * @param prefix    前缀
     * @param key       逻辑key
     * @param delta     减少量
     * @return          减少后的结果
     */
    public long decr(KeyPrefix prefix,String key,long delta)
    {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().decrement(realKey,delta);
    }

//**********************************************************************************************

    /**
     * 返回指定key的剩余生存时间
     * @param key
     * @return
     */
    public long getExpire(String key)
    {
        return redisTemplate.getExpire(key);
    }

    /**
     * 设置指定key的过期时间
     * @param key
     * @param timeout
     */
    public void setExpire(String key,long timeout)
    {
        redisTemplate.expire(key,timeout, TimeUnit.SECONDS);
    }


    /**
     * 实现命令：KEYS pattern，查找所有符合给定模式 pattern的 key
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 实现命令：DEL key，删除一个key
     * @param key
     */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    // String（字符串）
    /**
     * 实现命令：SET key value，设置一个key-value（将字符串值 value关联到 key）
     * @param key
     * @param value
     */
    public void setStr(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 实现命令：SET key value EX seconds，设置key-value和超时时间（秒）
     * @param key
     * @param value
     * @param timeout
     */
    public void setStr(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 实现命令：GET key，返回 key所关联的字符串值。
     * @param key
     * @return value
     */
    public String getStr(String key) {
        return (String)redisTemplate.opsForValue().get(key);
    }

    // Hash（哈希表）

    /**
     * 实现命令：HSET key field value，将哈希表 key中的域 field的值设为 value
     * @param key
     * @param field
     * @param value
     */
    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 实现命令：HGET key field，返回哈希表 key中给定域 field的值
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 实现命令：HDEL key field [field ...]，删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     * @param key
     * @param fields
     */
    public void hdel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 实现命令：HGETALL key，返回哈希表 key中，所有的域和值。
     * @param key
     * @return
     */
    public Map<Object, Object> hgetall(String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * 实现命令：LPUSH key value，将一个值 value插入到列表 key的表头
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long lpush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 实现命令：LPOP key，移除并返回列表 key的头元素。
     * @param key
     * @return 列表key的头元素。
     */
    public String lpop(String key)
    {
        return (String)redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 实现命令：RPUSH key value，将一个值 value插入到列表 key的表尾(最右边)。
     * @param key
     * @param value
     * @return 执行 LPUSH命令后，列表的长度。
     */
    public long rpush(String key, String value)
    {
        return redisTemplate.opsForList().rightPush(key, value);
    }
}
