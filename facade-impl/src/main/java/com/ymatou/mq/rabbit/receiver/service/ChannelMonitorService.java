package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.Channel;
import com.ymatou.mq.rabbit.receiver.support.ChannelMonitorTask;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * channel监听处理service
 * Created by zhangzhihua on 2017/3/31.
 */
@Component("channelMonitorService")
public class ChannelMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMonitorService.class);

    private List<ChannelWrapper> channelWrapperList = Collections.synchronizedList(new ArrayList<ChannelWrapper>());

    /**
     * 扫描并处理
     */
    public void scanAndProcess(){
        if(CollectionUtils.isEmpty(channelWrapperList)){
            return;
        }
        try {
            logger.info("current channel num:{}.",channelWrapperList.size());
            for(ChannelWrapper channelWrapper:channelWrapperList){
                Channel channel = channelWrapper.getChannel();
                Thread thread = channelWrapper.getThread();
                if(thread == null || !thread.isAlive()){
                    logger.warn("thread:{} is not alive.",thread);
                    if(channel != null){
                        channel.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("scanAndProcess occur error.",e);
        } catch (TimeoutException e) {
            logger.error("scanAndProcess occur error.",e);
        }
    }

    /**
     * 添加channel wrapper
     * @param channelWrapper
     */
    public void addChannerWrapper(ChannelWrapper channelWrapper){
        this.getChannelWrapperList().add(channelWrapper);
    }

    public List<ChannelWrapper> getChannelWrapperList() {
        return channelWrapperList;
    }

    public void setChannelWrapperList(List<ChannelWrapper> channelWrapperList) {
        this.channelWrapperList = channelWrapperList;
    }
}
