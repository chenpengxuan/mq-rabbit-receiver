package com.ymatou.mq.rabbit.receiver.task;

import com.rabbitmq.client.Channel;
import com.ymatou.mq.rabbit.RabbitChannelFactory;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * channel监听任务
 * Created by zhangzhihua on 2017/3/31.
 */
@Component
public class RabbitMonitorTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMonitorTask.class);

    @Override
    public void run() {
        try {
            logger.info("schedual invoke scanChannelAndProcess...");
            this.scanChannelAndProcess();
        } catch (Exception e) {
            logger.error("ChannelMonitorTask.run error.",e);
        }
    }

    /**
     * 扫描channel并处理
     */
    public void scanChannelAndProcess(){
        List<ChannelWrapper> channelWrapperList = RabbitChannelFactory.getChannelWrapperList();
        if(CollectionUtils.isEmpty(channelWrapperList)){
            return;
        }
        try {
            logger.info("current channel num:{}.",channelWrapperList.size());
            for(ChannelWrapper channelWrapper:channelWrapperList){
                Channel channel = channelWrapper.getChannel();
                Thread thread = channelWrapper.getThread();
                if(thread == null || !thread.isAlive()){
                    logger.debug("thread:{} is not alive,channel status:{}.",thread,channel != null?channel.isOpen():"null");
                    if(channel != null && channel.isOpen()){
                        //TODO 可以考虑复用
                        channel.close();
                        //conn.channel计数-1
                        channelWrapper.getConnectionWrapper().decCount();
                        channelWrapperList.remove(channelWrapper);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("scanAndProcess occur error.",e);
        } catch (TimeoutException e) {
            logger.error("scanAndProcess occur error.",e);
        }
    }

}
