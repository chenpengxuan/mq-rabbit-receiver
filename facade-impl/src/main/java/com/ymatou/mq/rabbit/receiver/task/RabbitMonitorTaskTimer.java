package com.ymatou.mq.rabbit.receiver.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;

/**
 * rabbit监听(如channel等)timer start
 * Created by zhangzhihua on 2017/3/31.
 */
@Component
public class RabbitMonitorTaskTimer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMonitorTask.class);

    @Autowired
    private RabbitMonitorTask channelMonitorTask;

    @PostConstruct
    public void init(){
        Timer timer = new Timer(true);
        timer.schedule(channelMonitorTask, 60, 1000 * 120);
        logger.info("monitor channel timer started.");
    }
}
