package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.SortedMap;

/**
 * rabbit ack监听扩展
 * Created by zhangzhihua on 2017/3/30.
 */
public class RabbitAckListener implements ConfirmListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitAckListener.class);

    /**
     * channel
     */
    private Channel channel;

    /**
     * 未确认集合
     */
    private SortedMap<Long, Message> unconfirmedSet;

    /**
     * rabbit分发facade
     */
    private RabbitDispatchFacade rabbitDispatchFacade;


    public RabbitAckListener(Channel channel, SortedMap<Long, Message> unconfirmedSet, RabbitDispatchFacade rabbitDispatchFacade){
        this.channel = channel;
        this.unconfirmedSet = unconfirmedSet;
        this.rabbitDispatchFacade = rabbitDispatchFacade;
        logger.debug("new RabbitAckListener,current thread name:{},thread id:{},channel:{},unconfirmedSet:{}",Thread.currentThread().getName(),Thread.currentThread().getId(),channel.hashCode(),unconfirmedSet);
    }

    @Override
    public void handleAck(long deliveryTag, boolean multiple) {
        logger.debug("handleAck,current thread name:{},thread id:{},deliveryTag:{},multiple:{},channel:{},unconfirmed：{}",Thread.currentThread().getName(),Thread.currentThread().getId(),deliveryTag,multiple,channel.hashCode(),unconfirmedSet);
        if (multiple) {
            unconfirmedSet.headMap(deliveryTag +1).clear();
        } else {
            logger.debug("first key:{},last key:{},values len:{}:",unconfirmedSet.firstKey(),unconfirmedSet.lastKey(),unconfirmedSet.size());
            unconfirmedSet.remove(deliveryTag);
        }
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        logger.error("handleNack,channel:{},deliveryTag:{},multiple:{}",channel,deliveryTag,multiple);
        logger.debug("handleAck,current thread name:{},thread id:{},deliveryTag:{},multiple:{},channel:{},unconfirmed：{}",Thread.currentThread().getName(),Thread.currentThread().getId(),deliveryTag,multiple,channel.hashCode(),unconfirmedSet);
        //若出现nack，则调用dispatch直接分发
        if (multiple) {
            //FIXME:直接取headMap()???
            long beginKey = unconfirmedSet.firstKey().longValue();
            for(long i=beginKey;i<deliveryTag+1;i++){
                Message message = unconfirmedSet.get(i);
                if(message != null){
                    //FIXME:异常了，继续continue??
                    rabbitDispatchFacade.dispatchMessage(message);
                }
            }
            unconfirmedSet.headMap(deliveryTag +1).clear();
        }else{
            try {
                //FIXME: unconfirmedSet.remove()
                Message message = unconfirmedSet.get(deliveryTag);
                if(message != null){
                    rabbitDispatchFacade.dispatchMessage(message);
                }

                unconfirmedSet.remove(deliveryTag);
                logger.debug("first key:{},last key:{},values len:",unconfirmedSet.firstKey(),unconfirmedSet.lastKey(),unconfirmedSet.size());
            } catch (Exception e) {
                logger.error("invoke dispatch fail.",e);
            }
        }

    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SortedMap<Long, Message> getUnconfirmedSet() {
        return unconfirmedSet;
    }

    public void setUnconfirmedSet(SortedMap<Long, Message> unconfirmedSet) {
        this.unconfirmedSet = unconfirmedSet;
    }

    public RabbitDispatchFacade getRabbitDispatchFacade() {
        return rabbitDispatchFacade;
    }

    public void setRabbitDispatchFacade(RabbitDispatchFacade rabbitDispatchFacade) {
        this.rabbitDispatchFacade = rabbitDispatchFacade;
    }
}
