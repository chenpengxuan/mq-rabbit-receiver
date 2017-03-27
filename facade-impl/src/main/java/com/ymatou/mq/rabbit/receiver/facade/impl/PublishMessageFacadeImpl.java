/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.facade.impl;



import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.service.RabbitReceiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author luoshiqian 2016/8/31 14:13
 */
@Service(protocol = "dubbo")
@Component
public class PublishMessageFacadeImpl implements PublishMessageFacade {

    private static final Logger logger = LoggerFactory.getLogger(PublishMessageFacadeImpl.class);

    @Autowired
    private RabbitReceiverService rabbitReceiverService;

    @Override
    public PublishMessageResp publish(PublishMessageReq req) {
        PublishMessageResp resp = new PublishMessageResp();
        //构造请求消息
        Message msg = this.buildMessage(req);
        //接收发布消息
        String msgUuid = rabbitReceiverService.receiveAndPublish(msg);
        resp.setUuid(msgUuid);
        return resp;
    }

    /**
     * 构造请求消息
     * @param req
     * @return
     */
    Message buildMessage(PublishMessageReq req){
        Message msg = new Message();
        return msg;
    }

}
