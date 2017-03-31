package com.ymatou.mq.rabbit.receiver.service;

import javax.annotation.PostConstruct;

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
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;

/**
 * rabbitmq接收消息service
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("rabbitReceiverService")
public class RabbitReceiverService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private MessageConfigService messageConfigService;

    @Autowired
    private FileQueueProcessorService fileQueueProcessorService;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private RabbitProducer rabbitProducer;

    /**
     * 接收并发布消息
     * @param msg
     * @return
     */
    public void receiveAndPublish(Message msg){
        //验证队列有效性
        this.validQueue(msg.getAppId(),msg.getQueueCode());
        //若rabbit master/slave都没开启，则直接调分发站
        if(!isEnableRabbit(rabbitConfig)){
            invokeDispatch(msg);
        }else{
            try {
                //发布消息
                rabbitProducer.publish(msg.getQueueCode(),msg);
            } catch(BizException e){ //FIXME: public过程中无BizException，直接去掉
                //若发布出现biz异常，则抛出由facade处理
                //FIXME:此处无需logger.error, FacadeAspect已处理
                logger.error("recevie and publish msg:{} occur biz exception.",msg,e);
                throw e;
            } catch (Exception e) {
                //若发布出现exception，则调用分发站
                logger.error("recevie and publish msg:{} occur exception.",msg,e);
                this.invokeDispatch(msg);
            }

            //FIXME:应该移到try{}内，publish成功发出，才需要写本地文件队列
            //若发MQ成功，则异步写消息到文件队列
            fileQueueProcessorService.saveMessageToFileDb(msg);
        }
    }

    /**
     * rabbit master/slave是否开启
     * @param rabbitConfig
     */
    boolean isEnableRabbit(RabbitConfig rabbitConfig){
        //FIXME: rabbitConfig.isMasterEnable() || rabbitConfig.isSlaveEnable()
        if(!rabbitConfig.isMasterEnable() && !rabbitConfig.isSlaveEnable()){
            return false;
        }
        return true;
    }

    /**
     * 直接调用分发站发送
     * @param message
     */
    void invokeDispatch(Message message){
        try {
            //若发MQ失败，则直接调用dispatch分发站接口发送
            rabbitDispatchFacade.dispatchMessage(message);
        } catch (Exception ex) {
            //发MQ失败->调分发站失败则返回失败信息
            throw new BizException(ErrorCode.FAIL,"invoke dispatcher send msg error",ex);
        }
    }

    /**
     * 验证queuCode有效性
     */
    void validQueue(String appId,String queueCode){
        QueueConfig queueConfig = messageConfigService.getQueueConfig(appId, queueCode);
        if(queueConfig == null){
            throw new BizException(ErrorCode.QUEUE_CONFIG_NOT_EXIST,String.format("appId:[%s],queueCode:[%s] not exist.",appId, queueCode));
        }
    }

    public RabbitConfig getRabbitConfig() {
        return rabbitConfig;
    }
}
