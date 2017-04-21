package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.dispatcher.facade.MessageDispatchFacade;
import com.ymatou.mq.rabbit.dispatcher.facade.model.DispatchMessageReq;
import com.ymatou.mq.rabbit.dispatcher.facade.model.DispatchMessageResp;
import com.ymatou.mq.rabbit.support.ChannelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private SortedMap<Long, Object> unconfirmedMap;

    /**
     * rabbit分发facade
     */
    private MessageDispatchFacade messageDispatchFacade;


    public RabbitAckListener(ChannelWrapper channelWrapper, MessageDispatchFacade messageDispatchFacade){
        this.channel = channelWrapper.getChannel();
        this.unconfirmedMap = channelWrapper.getUnconfirmedMap();
        this.messageDispatchFacade = messageDispatchFacade;
        logger.debug("new RabbitAckListener,current thread name:{},thread id:{},channel:{},unconfirmedMap:{}",Thread.currentThread().getName(),Thread.currentThread().getId(),channel.hashCode(),unconfirmedMap);
    }

    @Override
    public void handleAck(long deliveryTag, boolean multiple) {
        if (multiple) {
            unconfirmedMap.headMap(deliveryTag +1).clear();
        } else {
            logger.debug("first key:{},last key:{},values len:{}:",unconfirmedMap.firstKey(),unconfirmedMap.lastKey(),unconfirmedMap.size());
            unconfirmedMap.remove(deliveryTag);
        }
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        logger.warn("occur nack,channel:{},deliveryTag:{},multiple:{}",channel,deliveryTag,multiple);
        //若出现nack，则调用dispatch直接分发
        if (multiple) {
            for(Object object:unconfirmedMap.headMap(deliveryTag+1).values()){
                Message message = (Message)object;
                if(message != null){
                    try {
                        dispatchMessage(message);
                    } catch (Exception e) {
                        logger.error("dispatchMessage {} error.",message,e);
                    }
                }
            }
            unconfirmedMap.headMap(deliveryTag +1).clear();
        }else{
            try {
                Message message = (Message) unconfirmedMap.get(deliveryTag);
                if(message != null){
                    try {
                        dispatchMessage(message);
                    } catch (Exception e) {
                        logger.error("dispatchMessage {} error.",message,e);
                    }
                }
                unconfirmedMap.remove(deliveryTag);
                logger.debug("first key:{},last key:{},values len:",unconfirmedMap.firstKey(),unconfirmedMap.lastKey(),unconfirmedMap.size());
            } catch (Exception e) {
                logger.error("invoke dispatch fail.",e);
            }
        }

    }

    /**
     * 直接调用分发站发送
     * @param message
     */
    void dispatchMessage(Message message){
        logger.info("invoke dispatch,message:{}.",message);
        try {
            //若发MQ失败，则直接调用dispatch分发站接口发送
            DispatchMessageResp resp = messageDispatchFacade.dispatch(this.toDispatchMessageReq(message));
            if(!resp.isSuccess()){
                throw new BizException(ErrorCode.FAIL,resp.getErrorMessage());
            }
        } catch (Exception ex) {
            //发MQ失败->调分发站失败则返回失败信息
            throw new BizException(ErrorCode.FAIL,"dispatch message error",ex);
        }
    }

    /**
     * 转化为DispatchMessageReq
     * @param message
     * @return
     */
    DispatchMessageReq toDispatchMessageReq(Message message){
        DispatchMessageReq req = new DispatchMessageReq();
        req.setId(message.getId());
        req.setAppId(message.getAppId());
        req.setCode(message.getQueueCode());
        req.setMsgUniqueId(message.getBizId());
        req.setBody(message.getBody());
        req.setIp(message.getClientIp());
        return req;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SortedMap<Long, Object> getunconfirmedMap() {
        return unconfirmedMap;
    }

    public void setunconfirmedMap(SortedMap<Long, Object> unconfirmedMap) {
        this.unconfirmedMap = unconfirmedMap;
    }

    public MessageDispatchFacade getMessageDispatchFacade() {
        return messageDispatchFacade;
    }

    public void setMessageDispatchFacade(MessageDispatchFacade messageDispatchFacade) {
        this.messageDispatchFacade = messageDispatchFacade;
    }
}
