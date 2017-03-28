package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.ConfirmListener;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private SortedMap<Long, MessageAndConfigWrapper> unconfirmedSet = Collections.synchronizedSortedMap(new TreeMap<Long, MessageAndConfigWrapper>());

    /**
     * 根据appId/queueCode获取listener
     * @param appId
     * @param queueCode
     * @return
     */
    public ConfirmListener getConfirmListener(String appId, String queueCode){
        String confirmKey = String.format("%s_%s", appId, queueCode);
        if(confirmListenerMap.get(confirmKey) != null){
            return confirmListenerMap.get(confirmKey);
        }else{
            ConfirmListener confirmListener = new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    logger.info("ack " + "multiple:" + multiple + " tag:" + deliveryTag);
                    if (multiple) {
                        unconfirmedSet.headMap(deliveryTag +1).clear();
                    } else {
                        MessageAndConfigWrapper wrapper = unconfirmedSet.get(deliveryTag);
                        Message message = wrapper.getMessage();
                        //TODO 处理ack事件
                        unconfirmedSet.remove(deliveryTag);
                    }
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    logger.error("nack:" + deliveryTag + "," + multiple);
                    if (multiple) {

                    }else{
                        MessageAndConfigWrapper wrapper = unconfirmedSet.get(deliveryTag);
                        Message message = wrapper.getMessage();
                        //TODO 处理nack事件
                    }
                }
            };
            confirmListenerMap.put(confirmKey, confirmListener);
            return confirmListener;
        }
    }

    /**
     * MessageWrapper，用于包装Message,MessageConfig
     * @author zhangzhihua
     */
    class MessageAndConfigWrapper{

        private Message message;

        private QueueConfig queueConfig;

        public MessageAndConfigWrapper(Message message,QueueConfig queueConfig){
            this.message = message;
            this.queueConfig = queueConfig;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public QueueConfig getQueueConfig() {
            return queueConfig;
        }

        public void setQueueConfig(QueueConfig queueConfig) {
            this.queueConfig = queueConfig;
        }
    }
}