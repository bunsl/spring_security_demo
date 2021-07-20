package com.wang.demo.component.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 开启异步配置
 * @author wangjianhua
 * @date 2021/6/17/017 17:49
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 6;

    private static final int MAX_POOL_SIZE = 10;

    private static final int QUEUE_CAPACITY = 100;

    @Bean
    public Executor taskExecutor(){
        //默认配置  核心线程数大小为1 最大线程大小不受限制  队列容量也不受限制
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        //最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        //队列大小
        executor.setQueueCapacity(QUEUE_CAPACITY);
        //队列满时 此策略保证不会丢失任务  但是可能影响性能
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("custom threadPoolTaskExecutor");
        executor.initialize();
        return executor;
    }
}
