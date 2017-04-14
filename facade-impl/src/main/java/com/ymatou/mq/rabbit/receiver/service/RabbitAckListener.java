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
    private SortedMap<Long, Object> unconfirmedSet;

    /**
     * rabbit分发facade
     */
    private MessageDispatchFacade messageDispatchFacade;


    public RabbitAckListener(ChannelWrapper channelWrapper, MessageDispatchFacade messageDispatchFacade){
        this.channel = channelWrapper.getChannel();
        this.unconfirmedSet = channelWrapper.getUnconfirmedSet();
        this.messageDispatchFacade = messageDispatchFacade;
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
        logger.debug("handleNack,current thread name:{},thread id:{},deliveryTag:{},multiple:{},channel:{},unconfirmed：{}",Thread.currentThread().getName(),Thread.currentThread().getId(),deliveryTag,multiple,channel.hashCode(),unconfirmedSet);
        //若出现nack，则调用dispatch直接分发
        if (multiple) {
            //FIXME:直接取headMap()???
            long beginKey = unconfirmedSet.firstKey().longValue();
            for(long i=beginKey;i<deliveryTag+1;i++){
                Message message = (Message) unconfirmedSet.get(i);
                if(message != null){
                    //FIXME:异常了，继续continue??
                    dispatchMessage(message);
                }
            }
            unconfirmedSet.headMap(deliveryTag +1).clear();
        }else{
            try {
                //FIXME: unconfirmedSet.remove()
                Message message = (Message) unconfirmedSet.get(deliveryTag);
                if(message != null){
                    dispatchMessage(message);
                }
                unconfirmedSet.remove(deliveryTag);
                logger.debug("first key:{},last key:{},values len:",unconfirmedSet.firstKey(),unconfirmedSet.lastKey(),unconfirmedSet.size());
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

    public SortedMap<Long, Object> getUnconfirmedSet() {
        return unconfirmedSet;
    }

    public void setUnconfirmedSet(SortedMap<Long, Object> unconfirmedSet) {
        this.unconfirmedSet = unconfirmedSet;
    }

    public MessageDispatchFacade getMessageDispatchFacade() {
        return messageDispatchFacade;
    }

    public void setMessageDispatchFacade(MessageDispatchFacade messageDispatchFacade) {
        this.messageDispatchFacade = messageDispatchFacade;
    }
}
