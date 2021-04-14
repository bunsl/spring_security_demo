package com.wang.demo.component.mybatis.cache;

import com.wang.demo.component.redis.RedisComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 完成spring与redis中对象的注入
 * @author wangjianhua
 * @date 2021-03-18 11:07
 */
@Component
public class MybatisRedisCacheTransfer {

    /**
     * 通过MybatisRedisCacheTransfer完成spring和mybatis中对象的注入
     * @param redisComponent redis组件
     */
    public MybatisRedisCacheTransfer(@Autowired RedisComponent redisComponent){
        MybatisRedisCache.rediscomponent = redisComponent;
    }
}
