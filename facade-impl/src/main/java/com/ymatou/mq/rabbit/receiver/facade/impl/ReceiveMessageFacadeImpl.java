/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.facade.impl;


import java.util.Date;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.messagebus.facade.ReceiveMessageFacade;
import com.ymatou.messagebus.facade.model.ReceiveMessageReq;
import com.ymatou.messagebus.facade.model.ReceiveMessageResp;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.util.NetUtil;
import com.ymatou.mq.rabbit.receiver.service.RabbitReceiverService;


/**
 * @author luoshiqian 2016/8/31 14:13
 */
//@Service(protocol = "dubbo")
@Component
public class ReceiveMessageFacadeImpl implements ReceiveMessageFacade {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveMessageFacadeImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private RabbitReceiverService rabbitReceiverService;

    @Override
    public ReceiveMessageResp publish(ReceiveMessageReq req) {
        //构造请求消息
        Message msg = this.buildMessage(req);

        //接收发布消息
        rabbitReceiverService.receiveAndPublish(msg);

        //返回
        ReceiveMessageResp resp = new ReceiveMessageResp();
        resp.setUuid(msg.getId());
        resp.setSuccess(true);
        return resp;
    }

    /**
     * 构造请求消息
     * @param req
     * @return
     */
    Message buildMessage(ReceiveMessageReq req){
        Message msg = new Message();
        msg.setAppId(req.getAppId());
        msg.setQueueCode(req.getCode());
        msg.setId(ObjectId.get().toString());
        msg.setBizId(req.getMsgUniqueId());
        msg.setBody(req.getBody());
        msg.setClientIp(req.getIp());
        msg.setRecvIp(NetUtil.getHostIp());
        msg.setCreateTime(new Date());
        return msg;
    }

}
