package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.RabbitChannelFactory;
import com.ymatou.mq.rabbit.config.RabbitConfig;
import com.ymatou.mq.rabbit.receiver.config.ReceiverConfig;
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import com.ymatou.mq.rabbit.support.RabbitConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.*;

/**
 * rabbit生产者
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("rabbitProducer")
public class RabbitProducer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitProducer.class);

    /**
     * rabbit配置信息
     */
    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private ReceiverConfig receiverConfig;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    /**
     * 发布消息
     * @param queue
     * @param message
     * @throws IOException
     */
    public void publish(String queue, Message message) throws IOException {
        String msgId = message.getId();
        String bizId = message.getBizId();
        String body = message.getBody();

        logger.debug("RabbitProducer.publish,current thread name:{},thread id:{}",Thread.currentThread().getName(),Thread.currentThread().getId());
        //获取channel
        ChannelWrapper channelWrapper = RabbitChannelFactory.getChannelWrapper(receiverConfig.getCurrentCluster(),rabbitConfig);
        Channel channel = channelWrapper.getChannel();
        //若是第一次创建channel，则初始化ack相关
        if(channelWrapper.getUnconfirmedSet() == null){
            //设置channel对应的unconfirmedSet、acklistener信息
            SortedMap<Long, Object> unconfirmedSet = Collections.synchronizedSortedMap(new TreeMap<Long, Object>());
            channelWrapper.setUnconfirmedSet(unconfirmedSet);

            RabbitAckListener rabbitAckListener = new RabbitAckListener(channelWrapper,rabbitDispatchFacade);
            channel.addConfirmListener(rabbitAckListener);
            channel.confirmSelect();
        }

        //设置ack关联数据
        channelWrapper.getUnconfirmedSet().put(channel.getNextPublishSeqNo(),message);

        AMQP.BasicProperties basicProps = new AMQP.BasicProperties.Builder()
                .messageId(msgId).correlationId(bizId)
                .type(receiverConfig.getCurrentCluster()).deliveryMode(RabbitConstants.DELIVERY_PERSISTENT)
                .build();

        //FIXME:中文等非Ascii码传输，有编码问题吗
        channel.basicPublish("", queue, basicProps, SerializationUtils.serialize(body));
    }

}
