package com.ymatou.mq.rabbit.receiver.support;

import com.ymatou.mq.rabbit.receiver.service.ChannelMonitorService;
import com.ymatou.mq.rabbit.receiver.service.RabbitReceiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.Timer;
import java.util.TimerTask;

/**
 * channel监听任务
 * Created by zhangzhihua on 2017/3/31.
 */
public class ChannelMonitorTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMonitorTask.class);

    private ChannelMonitorService channelMonitorService;

    @Override
    public void run() {
        if(channelMonitorService == null){
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            channelMonitorService = (ChannelMonitorService)wac.getBean("channelMonitorService");
        }

        try {
            channelMonitorService.scanAndProcess();
        } catch (Exception e) {
            logger.error("ChannelMonitorTask.scanAndProcess error.",e);
        }
    }

}
