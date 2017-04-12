package com.ymatou.mq.rabbit.receiver.task;

import com.ymatou.mq.rabbit.receiver.service.RabbitMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;

/**
 * FIXME: 类太多了，ChannelMonitorTask/RabbitMonitorServer能否去掉？直接集成到这个类？
 * rabbit监听(如channel等)timer start
 * Created by zhangzhihua on 2017/3/31.
 */
@Component
public class RabbitMonitorTaskTimer {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMonitorTask.class);

    private ChannelMonitorTask channelMonitorTask;

    @Autowired
    private RabbitMonitorService rabbitMonitorService;

    @PostConstruct
    public void init(){
        if(channelMonitorTask == null){
            channelMonitorTask = new ChannelMonitorTask();
            channelMonitorTask.setRabbitMonitorService(rabbitMonitorService);
        }

        //FIXME: 为什么要try/catch??
//        try {
//            Timer timer = new Timer(true);
//            timer.schedule(channelMonitorTask, 0, 1000 * 10);
//            logger.info("monitor channel timer started.");
//        } catch (Exception e) {
//            logger.error("schedule error.",e);
//        }
    }
}
