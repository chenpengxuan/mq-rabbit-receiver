/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.fastjson.JSONObject;
import com.ymatou.messagebus.facade.model.ReceiveMessageReq;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.util.NetUtil;
import com.ymatou.mq.rabbit.receiver.BaseTest;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.fail;

/**
 * Created by zhangzhihua on 2017/3/29.
 */
public class RabbitReceiverServiceTest extends BaseTest{

    @Autowired
    RabbitReceiverService rabbitReceiverService;

    @Test
    public void testReceiveAndPublish(){
        for(int j=0;j<10;j++){
            try {
                ReceiveMessageReq req = new ReceiveMessageReq();
                req.setAppId("rabbit_optimization");
                req.setCode("biz1");
                req.setMsgUniqueId(ObjectId.get().toString());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name","kaka-" + new Date());
                req.setBody(jsonObject.toJSONString());
                req.setIp("172.16.22.102");
                Message msg = buildMessage(req);
                rabbitReceiverService.receiveAndPublish(msg);
            } catch (Exception e) {
                e.printStackTrace();
                fail("ReceiveAndPublish fail.");
            }
        }

        try {
            Thread.sleep(1000*60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
