package com.ymatou.mq.rabbit.receiver.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TimerTask;

/**
 * 消息配置定时刷新任务
 * Created by zhangzhihua on 2017/3/24.
 */
public class MessageConfigRefreshTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(MessageConfigRefreshTask.class);

    @Autowired
    private MessageConfigService messageConfigService;

    @Override
    public void run() {
        try {
            messageConfigService.loadConfig();
            if(logger.isInfoEnabled()){
                logger.info("reload config success...");
            }
        } catch (Exception e) {
            logger.error("reload config failed...",e);
        }
    }
}
