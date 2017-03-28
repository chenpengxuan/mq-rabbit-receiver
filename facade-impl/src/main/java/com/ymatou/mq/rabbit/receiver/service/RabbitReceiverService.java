package com.ymatou.mq.rabbit.receiver.service;

import com.rabbitmq.client.ConfirmListener;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.infrastructure.model.Message;

import com.ymatou.mq.rabbit.config.RabbitConfig;
import com.ymatou.mq.rabbit.receiver.support.RabbitDispatchFacade;
import com.ymatou.mq.rabbit.RabbitProducer;
import com.ymatou.mq.rabbit.RabbitProducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
    private MessageService messageService;

    @Autowired
    private FileQueueProcessorService fileQueueProcessorService;

    @Autowired
    private RabbitAckHandlerService rabbitAckHandlerService;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    @Resource
    private TaskExecutor taskExecutor;

    @Autowired
    private RabbitConfig rabbitConfig;

    /**
     * 接收并发布消息
     * @param msg
     * @return
     */
    public void receiveAndPublish(Message msg){
        try {
            //验证队列有效性
            this.validQueue(msg.getAppId(),msg.getQueueCode());

            //获取rabbit ack事件监听
            ConfirmListener confirmListener = rabbitAckHandlerService.getConfirmListener(msg.getAppId(),msg.getQueueCode());
            //调rabbitmq发布消息
            RabbitProducer rabbitProducer = RabbitProducerFactory.createRabbitProducer(msg.getAppId(),msg.getQueueCode(),confirmListener,this.getRabbitConfig());
            rabbitProducer.publish(msg.getBody(),msg.getBizId(),msg.getId(), this.getRabbitConfig());
            //设置共享unconfirmed集合
            rabbitAckHandlerService.setUnconfirmedSet(rabbitProducer.getUnconfirmedSet());

            //若发MQ成功，则异步写消息到文件队列
            fileQueueProcessorService.saveMessageToFileDb(msg);
        } catch (Exception e) {
            //FIXME：IllegalArumentException和BizException，不需要调分发站，直接返回客户端失败?
            logger.warn("publish msg fail.",e);
            try {
                //若发MQ失败，则直接调用dispatch分发站接口发送
                rabbitDispatchFacade.dispatchMessage(msg);
            } catch (Exception ex) {
                //发MQ失败->调分发站失败则返回失败信息
                throw new BizException(ErrorCode.FAIL,"invoke dispatcher send msg error",ex);
            }
        }
    }

    /**
     * 验证queuCode有效性
     */
    void validQueue(String appId,String queueCode){
        QueueConfig queueConfig = messageConfigService.getQueueConfig(appId, queueCode);
        if(queueConfig == null){
            throw new BizException(ErrorCode.QUEUE_CONFIG_NOT_EXIST,String.format("appId:{},queueCode:{} not exist.",appId, queueCode));
        }
    }

    public RabbitConfig getRabbitConfig() {
        return rabbitConfig;
    }
}
