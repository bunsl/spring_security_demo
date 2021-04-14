package com.wang.demo.component.mybatis.cache;

import com.wang.demo.component.redis.RedisComponent;
import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 管理mybatis与redis 二级缓存
 * @author wangjianhua
 * @date 2021-03-18 10:40
 */
public class MybatisRedisCache implements Cache {
    private final static Logger logger = LoggerFactory.getLogger(MybatisRedisCache.class);
    public static RedisComponent rediscomponent;
    private final String id;


    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 构造器初始化id
     * @param id id
     */
    public MybatisRedisCache(String id) {
        this.id = id;
        logger.info("myBatisRedisCache"+id);
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 像二级缓存中写入数据
     * @param key redis key
     * @param value redis value
     */
    @Override
    public void putObject(Object key, Object value) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        logger.info("向redis中写入数据"+rediscomponent);
        rediscomponent.hset(id,key.toString(),value);
        writeLock.unlock();

    }

    /**
     * 从二级缓存中获取key
     * @param key redis key
     * @return redis value
     */
    @Override
    public Object getObject(Object key) {
        Lock readLock = readWriteLock.readLock();
        readLock.lock();
        Object object = rediscomponent.hget(id,key.toString());
        readLock.unlock();
        return object;
    }

    /**
     * 从二级缓存中删除数据
     * @param key redis key
     * @return 结果
     */
    @Override
    public Object removeObject(Object key) {
        try{
            rediscomponent.hdel(id,key);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 清空全部二级缓存的数据
     */
    @Override
    public void clear() {
        Set<String> keys = rediscomponent.keys();
        for (String key : keys) {
           if(key.equals(id)){
               rediscomponent.del(key);
           }
        }
        logger.error("<=====清空二级缓存数据=====>");
    }

    /**
     * 获取二级缓存中的总条数
     * @return 结果
     */
    @Override
    public int getSize() {
        return rediscomponent.keys().size();
    }

}
