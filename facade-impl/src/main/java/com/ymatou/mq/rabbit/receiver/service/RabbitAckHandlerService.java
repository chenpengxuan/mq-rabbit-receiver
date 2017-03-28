package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.ConfirmListener;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;
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
public class RabbitAckHandlerService{

    private static final Logger logger = LoggerFactory.getLogger(RabbitAckHandlerService.class);

    /**
     * ConfirmListener映射
     */
    private Map<String,ConfirmListener> confirmListenerMap = new ConcurrentHashMap<String,ConfirmListener>();

    /**
     * 未确认集合
     */
    private SortedMap<Long, Map<String,Object>> unconfirmedSet = null;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    /**
     * 获取confirm listener
     * @return
     */
    public ConfirmListener getConfirmListener(){
        /*
        String confirmKey = String.format("%s_%s", appId, queueCode);
        if(confirmListenerMap.get(confirmKey) != null){
            return confirmListenerMap.get(confirmKey);
        }else{
            ConfirmListener confirmListener = new DefaultConfirmListener();
            confirmListenerMap.put(confirmKey,confirmListener);
            return confirmListener;
        }
        */
        return new DefaultConfirmListener();
    }

    /**
     * 默认ConfirmListener处理
     */
    class DefaultConfirmListener implements ConfirmListener{
        @Override
        public void handleAck(long deliveryTag, boolean multiple) throws IOException {
            logger.debug("ack " + "multiple:" + multiple + " tag:" + deliveryTag);
            if (multiple) {
                unconfirmedSet.headMap(deliveryTag +1).clear();
            } else {
                unconfirmedSet.remove(deliveryTag);
            }
        }

        @Override
        public void handleNack(long deliveryTag, boolean multiple) throws IOException {
            logger.error("nack:" + deliveryTag + "," + multiple);
            if (multiple) {
                //TODO
            }else{
                //若出现nack，则调用dispatch直接分发
                try {
                    Map<String,Object> map = unconfirmedSet.get(deliveryTag);
                    rabbitDispatchFacade.dispatchMessage(getPublishMessage(map));
                } catch (Exception e) {
                    logger.error("invoke dispatch fail.",e);
                }
            }
        }
    }

    /**
     * 获取要发布的消息
     * @param map
     * @return
     */
    Message getPublishMessage(Map<String,Object> map){
        Message msg = new Message();
        msg.setQueueCode(String.valueOf(map.get(RabbitConstants.QUEUE_CODE)));
        msg.setId(String.valueOf(map.get(RabbitConstants.MSG_ID)));
        msg.setBizId(String.valueOf(map.get(RabbitConstants.BIZ_ID)));
        msg.setBody(String.valueOf(map.get(RabbitConstants.BODY)));
        return msg;
    }

    public SortedMap<Long, Map<String, Object>> getUnconfirmedSet() {
        return unconfirmedSet;
    }

    public void setUnconfirmedSet(SortedMap<Long, Map<String, Object>> unconfirmedSet) {
        this.unconfirmedSet = unconfirmedSet;
    }
}
