package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @program: sky-take-out
 * @description: 自定义定时Task
 * @author: {}
 * @create: 2024/4/21 17:23
 */
@Component
@Slf4j
public class MyTask {

    @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask(){
        log.info("定时任务开始执行",new Date());
    }
}
