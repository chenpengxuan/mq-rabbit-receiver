package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;
import com.ymatou.mq.rabbit.support.RabbitAckHandler;
import com.ymatou.mq.rabbit.support.RabbitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rabbit ack处理事件service
 * Created by zhangzhihua on 2017/3/27.
 */
@Component("rabbitAckHandlerService")
public class RabbitAckHandlerService implements RabbitAckHandler {

    private static final Logger logger = LoggerFactory.getLogger(RabbitAckHandlerService.class);

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    @Override
    public void handleAck(long deliveryTag, boolean multiple, Channel channel, SortedMap<Long, Message> unconfirmedSet) {
        logger.debug("handleAck,current thread name:{},thread id:{},deliveryTag:{},multiple:{},channel:{},unconfirmed：{}",Thread.currentThread().getName(),Thread.currentThread().getId(),deliveryTag,multiple,channel.hashCode(),unconfirmedSet);
        if (multiple) {
            unconfirmedSet.headMap(deliveryTag +1).clear();
        } else {
            logger.debug("first key:{},last key:{},values len:{}:",unconfirmedSet.firstKey(),unconfirmedSet.lastKey(),unconfirmedSet.size());
            unconfirmedSet.remove(deliveryTag);
        }
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple, Channel channel, SortedMap<Long, Message> unconfirmedSet) throws IOException {
        //FIXME: nack要记error
        logger.debug("handleAck,current thread name:{},thread id:{},deliveryTag:{},multiple:{},channel:{},unconfirmed：{}",Thread.currentThread().getName(),Thread.currentThread().getId(),deliveryTag,multiple,channel.hashCode(),unconfirmedSet);
        //若出现nack，则调用dispatch直接分发
        if (multiple) {
            long beginKey = unconfirmedSet.firstKey().longValue();
            for(long i=beginKey;i<deliveryTag+1;i++){
                Message message = unconfirmedSet.get(i);
                if(message != null){
                    rabbitDispatchFacade.dispatchMessage(message);
                }
            }
            unconfirmedSet.headMap(deliveryTag +1).clear();
        }else{
            try {
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


}
