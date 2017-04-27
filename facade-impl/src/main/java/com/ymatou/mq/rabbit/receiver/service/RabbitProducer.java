package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.rabbit.RabbitChannelFactory;
import com.ymatou.mq.rabbit.config.RabbitConfig;
import com.ymatou.mq.rabbit.dispatcher.facade.MessageDispatchFacade;
import com.ymatou.mq.rabbit.receiver.config.ReceiverConfig;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import com.ymatou.mq.rabbit.support.RabbitConstants;
import org.apache.commons.lang3.StringUtils;
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
    private MessageConfigService messageConfigService;

    @Reference
    private MessageDispatchFacade messageDispatchFacade;

    /**
     * 发布消息
     * @param exchange
     * @param message
     * @throws IOException
     */
    public void publish(String exchange, Message message) throws IOException {
        String msgId = message.getId();
        String bizId = message.getBizId();
        //获取channel
        ChannelWrapper channelWrapper = RabbitChannelFactory.getChannelWrapperByThreadContext(receiverConfig.getCurrentCluster(),rabbitConfig);
        Channel channel = channelWrapper.getChannel();
        //若是第一次创建channel，则初始化ack相关
        if(channelWrapper.getUnconfirmedMap() == null){
            //设置channel对应的unconfirmedSet、acklistener信息
            SortedMap<Long, Object> unconfirmedSet = Collections.synchronizedSortedMap(new TreeMap<Long, Object>());
            channelWrapper.setUnconfirmedMap(unconfirmedSet);

            RabbitAckListener rabbitAckListener = new RabbitAckListener(channelWrapper,messageDispatchFacade);
            channel.addConfirmListener(rabbitAckListener);
            channel.confirmSelect();
        }

        //设置ack关联数据
        channelWrapper.getUnconfirmedMap().put(channel.getNextPublishSeqNo(),message);

        AMQP.BasicProperties basicProps = new AMQP.BasicProperties.Builder()
                .messageId(msgId).correlationId(bizId)
                .deliveryMode(RabbitConstants.DELIVERY_PERSISTENT)
                .build();

        String routeKey = getRouteKey(message.getAppId(),message.getQueueCode());
        if(StringUtils.isNoneBlank(routeKey)){
            long startTime = System.currentTimeMillis();
            channel.basicPublish(exchange, routeKey, basicProps, toBytesByJava(message));
            long costTime = System.currentTimeMillis()-startTime;

            if(costTime > 1000){
                logger.warn("publish message to MQ slow gt 1000ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 500){
                logger.warn("publish message to MQ slow gt 500ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 200){
                logger.warn("publish message to MQ slow gt 200ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 100){
                logger.warn("publish message to MQ slow gt 100ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 50){
                logger.warn("publish message to MQ slow gt 50ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 20){
                logger.warn("publish message to MQ slow gt 20ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else if(costTime > 10){
                logger.warn("publish message to MQ slow gt 10ms,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }else {
                logger.info("publish message to MQ,consume:{},exchange:{},routeKey:{}.",costTime,exchange,routeKey);
            }
        }
    }

    /**
     * 通过java序列化
     * @param message
     * @return
     */
    byte[] toBytesByJava(Message message){
        long startTime = System.currentTimeMillis();
        byte[] bytes = SerializationUtils.serialize(message);
        logger.debug("seriable bytes by java consume:{}.",System.currentTimeMillis()-startTime);
        return bytes;
    }

    /**
     * 通过fastjson序列化
     * @param message
     * @return
     */
    byte[] toBytesByFastJson(Message message){
        long startTime = System.currentTimeMillis();
        //默认UTF-8 byte编码
        SerializeConfig serializeConfig = new SerializeConfig();
        SerializerFeature[] serializerFeatures = {};
        byte[] bytes =  JSON.toJSONBytes(message,serializeConfig,serializerFeatures);
        logger.debug("seriable bytes by fastjson consume:{}.",System.currentTimeMillis()-startTime);
        return bytes;
    }

    /**
     * 获取routeKey
     * @param appId
     * @param queueCode
     * @return
     */
    String getRouteKey(String appId,String queueCode){
        StringBuffer buf = new StringBuffer();
        List<CallbackConfig> callbackConfigList = messageConfigService.getCallbackConfigList(appId,queueCode);
        int i = 0;
        for(CallbackConfig callbackConfig:callbackConfigList){
            if(callbackConfig.isDispatchEnable()){
                if(i == 0){
                    buf.append(getCallbackNo(callbackConfig.getCallbackKey()));
                }else{
                    buf.append(String.format(".%s",getCallbackNo(callbackConfig.getCallbackKey())));
                }
                i++;
            }
        }
        return buf.toString().trim();
    }

    /**
     * 获取callbackKey序号
     * @param callbackKey
     * @return
     */
    String getCallbackNo(String callbackKey){
        return callbackKey.substring(callbackKey.lastIndexOf("_")+1,callbackKey.length());
    }

}
