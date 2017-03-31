package com.ymatou.mq.rabbit.receiver.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;

/**
 * rabbit监听(如channel等)timer start
 * Created by zhangzhihua on 2017/3/31.
 */
@Component
public class RabbitMonitorTaskTimer {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMonitorTask.class);

    private ChannelMonitorTask channelMonitorTask;

    @PostConstruct
    public void init(){
        if(channelMonitorTask == null){
            channelMonitorTask = new ChannelMonitorTask();
        }

        try {
            Timer timer = new Timer(true);
            timer.schedule(channelMonitorTask, 0, 1000 * 10);
            logger.info("monitor channel timer started.");
        } catch (Exception e) {
            logger.error("schedule error.",e);
        }
    }
}
