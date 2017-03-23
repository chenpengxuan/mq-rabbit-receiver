package com.ymatou.mq.rabbit.receiver.service;

import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.rabbit.receiver.model.Message;
import com.ymatou.mq.rabbit.receiver.repository.MessageRepository;
import com.ymatou.mq.rabbit.receiver.third.FileDb;
import com.ymatou.mq.rabbit.receiver.third.RabbitDispatchFacade;
import com.ymatou.mq.rabbit.receiver.third.RabbitProducer;
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
    private RabbitProducer rabbitProducer;

    @Autowired
    private FileDb fileDb;

    @Autowired
    private MessageRepository messageRepository;

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
            //调rabbitmq发布消息
            rabbitProducer.publish(msg);
            //若发MQ成功，则异步写消息到文件队列
            //taskExecutor.execute(()-> this.writeMessageToFileQueue(msg));
            this.writeMessageToFileQueue(msg);
        } catch (Exception e) {
            logger.error("publish msg {} error",msg,e);
            try {
                //若发MQ失败，则直接调用dispatch分发站接口发送
                rabbitDispatchFacade.dispatchMessage(msg);
            } catch (Exception ex) {
                throw new BizException(ErrorCode.FAIL,"invoke dispatcher send msg {} error",ex);
            }
        }
        return msg.getMsgUuid();
    }

    /**
     * 写消息到本地文件队列
     * @param msg
     */
    void writeMessageToFileQueue(Message msg){
        try {
            //TODO fileDb要同步写，否则mongo无法写
            fileDb.saveMessage(msg);
        } catch (Exception e) {
            logger.error("write msg {} to local file queue error.",msg,e);
            try {
                messageRepository.saveMessage(msg);
            } catch (Exception ex) {
                logger.error("write msg {} to mongo error.",msg,ex);
            }
        }
    }
}
