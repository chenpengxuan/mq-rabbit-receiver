package com.ymatou.mq.rabbit.receiver.service;

import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.rabbit.receiver.model.Message;
import com.ymatou.mq.rabbit.receiver.model.QueueConfig;
import com.ymatou.mq.rabbit.receiver.repository.MessageRepository;
import com.ymatou.mq.rabbit.receiver.third.filequeue.FileDb;
import com.ymatou.mq.rabbit.receiver.third.dispatcher.RabbitDispatchFacade;
import com.ymatou.mq.rabbit.receiver.third.rabbit.RabbitProducer;
import com.ymatou.mq.rabbit.receiver.third.rabbit.RabbitProducerFactory;
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
    private FileDb fileDb;

    @Autowired
    private RabbitDispatchFacade rabbitDispatchFacade;

    @Resource
    private TaskExecutor taskExecutor;

    /**
     * 接收并发布消息
     * @param msg
     * @return
     */
    public String receiveAndPublish(Message msg){
        try {
            //验证队列/bizCode有效性
            this.validQueue(msg.getAppId(),msg.getBizCode());

            //调rabbitmq发布消息
            RabbitProducer rabbitProducer = RabbitProducerFactory.createRabbitProducer(msg.getAppId(),msg.getBizCode());
            rabbitProducer.publish(msg);

            //若发MQ成功，则异步写消息到文件队列
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    writeMessageToFileQueue(msg);
                }
            });
        } catch (Exception e) {
            logger.error("publish msg {} error",msg,e);
            try {
                //若发MQ失败，则直接调用dispatch分发站接口发送
                rabbitDispatchFacade.dispatchMessage(msg);
            } catch (Exception ex) {
                //发MQ失败->调分发站失败则返回失败信息
                throw new BizException(ErrorCode.FAIL,"invoke dispatcher send msg {} error",ex);
            }
        }
        return msg.getMsgUuid();
    }

    /**
     * 验证bizCode/队列有效性
     */
    void validQueue(String appId,String bizCode){
        QueueConfig queueConfig = messageConfigService.getQueueConfig(appId,bizCode);
        if(queueConfig == null){
            throw new BizException(ErrorCode.QUEUE_CONFIG_NOT_EXIST,String.format("appId:{},bizCode:{} queue config not exist.",appId,bizCode));
        }
    }

    /**
     * 写消息到本地文件队列
     * @param msg
     */
    void writeMessageToFileQueue(Message msg){
        try {
            fileDb.saveMessage(msg);
        } catch (Exception e) {
            logger.error("write msg {} to local file queue error.",msg,e);
            try {
                //若写本地文件队列异常，则直接写mongo(消息及分发明细)
                //TODO 通过写异常回调事件
                messageService.saveMessage(msg);
            } catch (Exception ex) {
                logger.error("write msg {} to mongo error.",msg,ex);
            }
        }
    }
}
