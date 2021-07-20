package com.wang.demo.component.async.controller;

import com.wang.demo.base.response.ResultMessage;
import com.wang.demo.component.async.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author wangjianhua
 * @date 2021/6/17/017 18:11
 */
@RestController
@RequestMapping("async")
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("movies")
    public String  completableFutureTask(){
        //开始时间
        long start = System.currentTimeMillis();
        //开始执行大量异步任务
        List<String> words  = Arrays.asList("F","T","S","Z","J","C");
        List<CompletableFuture<List<String>>> completableFutureTask =
                words.stream()
                        .map(word -> asyncService.completableFutureTask(word))
                        .collect(Collectors.toList());
        List<List<String>> results =
                completableFutureTask.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        System.out.println("花费时间:" +(System.currentTimeMillis()-start));
        return results.toString();
    }

    @GetMapping("movie")
    public ResultMessage completableFutureTask1(){
        //开始时间
        long start = System.currentTimeMillis();
        //开始执行大量异步任务
        List<String> words  = Arrays.asList("F","T","S","Z","J","C");

                words.stream().forEach(word -> asyncService.completableFutureTask1(word));
        System.out.println("花费时间:" +(System.currentTimeMillis()-start));
        return ResultMessage.success("成功");
    }
}
