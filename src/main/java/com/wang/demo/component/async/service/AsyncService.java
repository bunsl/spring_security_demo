package com.wang.demo.component.async.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author wangjianhua
 * @date 2021/6/17/017 17:59
 */
@Service
public class AsyncService {
    private static final Logger logger   = LoggerFactory.getLogger(AsyncService.class);

    private List<String>  movies =
            new ArrayList<>(Arrays.asList("Forrest Gump," +
                    "  Titanic," +
                    "  Spirited Away," +
                    "  The Shawshank Redemption," +
                    "  Zootopia," +
                    "  Farewell ," +
                    "  Joker," +
                    "  Crawl"));

    @Async
    public CompletableFuture<List<String>>  completableFutureTask(String start){
        logger.warn(Thread.currentThread().getName()+"start this task");
        List<String> results = movies.stream()
                .filter(movie -> movie.startsWith(start)).collect(Collectors.toList());
        //模拟耗时
        try {
            Thread.sleep(1000L);
        }catch (InterruptedException e){
            logger.warn(e.getMessage());
        }

        //返回一个已经用给定值完成的新的CompletableFuture
        return CompletableFuture.completedFuture(results);
    }

    @Async
    public void completableFutureTask1(String start){
        logger.warn(Thread.currentThread().getName()+"start this task");
        List<String> results = movies.stream()
                .filter(movie -> movie.startsWith(start)).collect(Collectors.toList());
        //模拟耗时
        try {
            Thread.sleep(1000L);
        }catch (InterruptedException e){
            logger.warn(e.getMessage());
        }

    }
}
