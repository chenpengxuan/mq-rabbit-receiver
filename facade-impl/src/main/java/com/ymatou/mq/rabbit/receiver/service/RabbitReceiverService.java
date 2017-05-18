package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ymatou.mq.rabbit.dispatcher.facade.MessageDispatchFacade;
import com.ymatou.mq.rabbit.dispatcher.facade.model.DispatchMessageReq;
import com.ymatou.mq.rabbit.dispatcher.facade.model.DispatchMessageResp;
import com.ymatou.mq.rabbit.receiver.config.ReceiverConfig;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.rabbit.config.RabbitConfig;

import java.util.Random;

/**
 * rabbitmq接收消息service
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("rabbitReceiverService")
public class RabbitReceiverService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitReceiverService.class);

    public static final String MONITOR_APP_ID = "monitor.mq.iapi.ymatou.com";

    @Autowired
    private MessageConfigService messageConfigService;

    @Autowired
    private MessageFileQueueService messageFileQueueService;

    @Reference
    private MessageDispatchFacade messageDispatchFacade;

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private ReceiverConfig receiverConfig;

    @Autowired
    private RabbitProducer rabbitProducer;

    /**
     * 接收并发布消息
     * @param message
     * @return
     */
    public void receiveAndPublish(Message message){
        logger.info("receive message:{}.", message);

        //验证队列有效性
        this.validQueue(message.getAppId(), message.getQueueCode());

        //若rabbit master/slave都没开启，则直接调分发站
        if(!isEnableRabbit()){
            dispatchMessage(message);
        }else{
            try {
                //发布消息
                rabbitProducer.publish(String.format("%s_%s", message.getAppId(), message.getQueueCode()), message);
                Thread.sleep(new Random().nextInt(100));
                //若发MQ成功，则异步写消息到文件队列
                //messageFileQueueService.saveMessageToFileDb(message);
            } catch (Exception e) {
                //若发布出现exception，则调用分发站
                logger.error("recevie and publish msg:{} occur exception.", message,e);
                dispatchMessage(message);
            }

        }
    }

    /**
     * rabbit master/slave是否开启
     */
    boolean isEnableRabbit(){
        if(receiverConfig.isMasterEnable() || receiverConfig.isSlaveEnable()){
            return true;
        }
        return false;
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
        req.setApp(message.getAppId());
        req.setCode(message.getQueueCode());
        req.setMsgUniqueId(message.getBizId());
        req.setBody(message.getBody());
        req.setIp(message.getClientIp());
        return req;
    }

    /**
     * 验证queuCode有效性
     */
    void validQueue(String appId,String queueCode){
        QueueConfig queueConfig = messageConfigService.getQueueConfig(appId, queueCode);
        if(queueConfig == null){
            throw new BizException(ErrorCode.QUEUE_CONFIG_NOT_EXIST,String.format("appId:[%s],queueCode:[%s] not exist.",appId, queueCode));
        }
        if(!queueConfig.getEnable()){
            throw new BizException(ErrorCode.QUEUE_CONFIG_NOT_ENABLE,String.format("appId:[%s],queueCode:[%s] not enabled.",appId, queueCode));
        }
    }

    public RabbitConfig getRabbitConfig() {
        return rabbitConfig;
    }
}
