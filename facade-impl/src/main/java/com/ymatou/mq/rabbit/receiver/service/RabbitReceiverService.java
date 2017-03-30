package com.ymatou.mq.rabbit.receiver.service;

import javax.annotation.PostConstruct;

import com.ymatou.mq.rabbit.RabbitChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.rabbit.RabbitProducer;
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
    private RabbitAckHandlerService rabbitAckHandlerService;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private RabbitProducer rabbitProducer;

    @PostConstruct
    public void init(){
        //设置ack 回调处理handler
        rabbitProducer.setRabbitAckHandler(rabbitAckHandlerService);
    }

    /**
     * 接收并发布消息
     * @param msg
     * @return
     */
    //FIXME 返回要带上成功失败 不能一直是成功
    public void receiveAndPublish(Message msg){
        try {
            //验证队列有效性
            this.validQueue(msg.getAppId(),msg.getQueueCode());

            //若rabbit master/slave都没开启，则直接调分发站
            if(!isEnableRabbit(rabbitConfig)){
                invokeDispatch(msg);
            }else{
                //发布消息
                rabbitProducer.publish(msg.getQueueCode(),msg);
                //若发MQ成功，则异步写消息到文件队列
                fileQueueProcessorService.saveMessageToFileDb(msg);
            }

        } catch(BizException e){
            //若出现biz异常，则抛出由facade处理
            logger.error("recevie and publish msg:{} occur biz exception.",msg,e);
            throw e;
        } catch (Exception e) {
            //若出现exception，则调用分发站
            logger.error("recevie and publish msg:{} occur exception.",msg,e);

            //FIXME 根据返回值成功 失败
            this.invokeDispatch(msg);
        }
    }

    /**
     * rabbit master/slave是否开启
     * @param rabbitConfig
     */
    boolean isEnableRabbit(RabbitConfig rabbitConfig){
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
