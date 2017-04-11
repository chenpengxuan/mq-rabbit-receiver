package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.Channel;
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
 * rabbit监听(如channel等)处理service
 * Created by zhangzhihua on 2017/3/31.
 */
@Component("rabbitMonitorService")
public class RabbitMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMonitorService.class);

    /**
     * channel wrapper列表
     */
    private List<ChannelWrapper> channelWrapperList = Collections.synchronizedList(new ArrayList<ChannelWrapper>());

    /**
     * 扫描channel并处理
     */
    public void scanChannelAndProcess(){
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
                        this.clearChannelWrapper(channelWrapper);
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
     * 清理channel wrapper
     * @param channelWrapper
     */
    public void clearChannelWrapper(ChannelWrapper channelWrapper){
        //删除channelWrapperList中元素
        if(!CollectionUtils.isEmpty(channelWrapperList)){
            //FIXME: channelWrapperList.remove(channelWrapper)??
            for(ChannelWrapper item:channelWrapperList){
                if(item.getChannel() == channelWrapper.getChannel()){
                    channelWrapperList.remove(channelWrapper);
                }
            }
        }
        //conn.channel计数-1
        channelWrapper.getConnectionWrapper().decCount();
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
