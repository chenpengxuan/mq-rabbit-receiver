/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.fastjson.JSONObject;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.util.NetUtil;
import com.ymatou.mq.rabbit.receiver.BaseTest;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by zhangzhihua on 2017/3/29.
 */
public class RabbitReceiverServiceTest extends BaseTest{

    @Autowired
    RabbitReceiverService rabbitReceiverService;

    @Test
    public void testReceiveAndPublish(){
        try {
            for(int i=0;i<5;i++){
                PublishMessageReq req = new PublishMessageReq();
                req.setAppId("rabbit_optimization");
                req.setCode("biz1");
                req.setMsgUniqueId(ObjectId.get().toString());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name","kaka");
                req.setBody(jsonObject.toJSONString());
                req.setIp("172.16.22.102");
                Message msg = buildMessage(req);
                rabbitReceiverService.receiveAndPublish(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造请求消息
     * @param req
     * @return
     */
    Message buildMessage(PublishMessageReq req){
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
