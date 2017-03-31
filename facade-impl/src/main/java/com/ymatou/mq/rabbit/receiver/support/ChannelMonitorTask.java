package com.ymatou.mq.rabbit.receiver.support;

import com.ymatou.mq.rabbit.receiver.service.RabbitMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.TimerTask;

/**
 * channel监听任务
 * Created by zhangzhihua on 2017/3/31.
 */
public class ChannelMonitorTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMonitorTask.class);

    private RabbitMonitorService rabbitMonitorService;

    @Override
    public void run() {
        if(rabbitMonitorService == null){
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            rabbitMonitorService = (RabbitMonitorService)wac.getBean("rabbitMonitorService");
        }

        try {
            rabbitMonitorService.scanChannelAndProcess();
        } catch (Exception e) {
            logger.error("ChannelMonitorTask.run error.",e);
        }
    }

}
