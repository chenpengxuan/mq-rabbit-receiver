package com.ymatou.mq.rabbit.receiver.task;

import com.rabbitmq.client.Channel;
import com.ymatou.mq.rabbit.RabbitChannelFactory;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import com.ymatou.mq.rabbit.support.RabbitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
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
            logger.debug("scanChannelAndProcess...");
            this.scanChannelAndProcess();
        } catch (Exception e) {
            logger.error("scanChannelAndProcess error.",e);
        }
    }

    /**
     * 扫描channel并处理
     */
    public void scanChannelAndProcess(){
        String[] clusters = {RabbitConstants.CLUSTER_MASTER,RabbitConstants.CLUSTER_SLAVE};
        for(String cluster:clusters){
            List<ChannelWrapper> channelWrapperList = RabbitChannelFactory.getChannelWrapperList(cluster);
            if(CollectionUtils.isEmpty(channelWrapperList)){
                continue;
            }
            try {
                logger.info("current cluster:{} channel num:{}.",cluster,channelWrapperList.size());
                for(ChannelWrapper channelWrapper:channelWrapperList){
                    Channel channel = channelWrapper.getChannel();
                    Thread thread = channelWrapper.getThread();
                    if(thread == null || !thread.isAlive()){
                        logger.info("thread:{} is not alive,channel status:{}.",thread,channel != null?channel.isOpen():"null");
                        if(channel != null && channel.isOpen()){
                            RabbitChannelFactory.releaseChannelWrapper(cluster,channelWrapper,false);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("scanAndProcess occur error.",e);
            }
        }
    }

}
