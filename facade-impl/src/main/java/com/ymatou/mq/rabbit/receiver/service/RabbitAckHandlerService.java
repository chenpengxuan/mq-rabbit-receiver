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
     * 未确认集合
     */
    private SortedMap<Long, Message> unconfirmedSet = Collections.synchronizedSortedMap(new TreeMap<Long, Message>());

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    /**
     * 获取confirm listener
     * @return
     */
    public ConfirmListener getConfirmListener(){
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
                unconfirmedSet.headMap(deliveryTag +1).clear();
            }else{
                //若出现nack，则调用dispatch直接分发
                try {
                    Message message = unconfirmedSet.get(deliveryTag);
                    rabbitDispatchFacade.dispatchMessage(message);
                    unconfirmedSet.remove(deliveryTag);
                } catch (Exception e) {
                    logger.error("invoke dispatch fail.",e);
                }
            }
        }
    }

    public SortedMap<Long, Message> getUnconfirmedSet() {
        return unconfirmedSet;
    }

    public void setUnconfirmedSet(SortedMap<Long, Message> unconfirmedSet) {
        this.unconfirmedSet = unconfirmedSet;
    }
}
