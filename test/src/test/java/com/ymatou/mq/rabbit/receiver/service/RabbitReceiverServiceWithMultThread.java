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
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.fail;

/**
 * Created by zhangzhihua on 2017/3/29.
 */
/**
 * @author luoshiqian 2017/3/27 16:34
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.ymatou")
public class RabbitReceiverServiceWithMultThread{

    private static final Logger logger = LoggerFactory.getLogger(RabbitReceiverServiceWithMultThread.class);

    AnnotationConfigApplicationContext ctx;

    RabbitReceiverService rabbitReceiverService;

    void init(){
        ctx = new AnnotationConfigApplicationContext(RabbitReceiverServiceWithMultThread.class);
        rabbitReceiverService = (RabbitReceiverService)ctx.getBean("rabbitReceiverService");
        logger.info("instance rabbitReceiverService:{}",rabbitReceiverService);
    }

    public void testReceiveAndPublishWithMulThread(){
        for(int i=0;i<2;i++){
            new Thread( new Runnable(){
                @Override
                public void run() {
                    for(int j=0;j<10;j++){
                        try {
                            PublishMessageReq req = new PublishMessageReq();
                            logger.info("current thread name:{},thread id:{}",Thread.currentThread().getName(),Thread.currentThread().getId());
                            req.setAppId("rabbit_optimization");
                            req.setCode("biz1");
                            req.setMsgUniqueId(ObjectId.get().toString());
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name","kaka");
                            req.setBody(jsonObject.toJSONString());
                            req.setIp("172.16.22.102");
                            Message msg = buildMessage(req);
                            rabbitReceiverService.receiveAndPublish(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
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

    public static void main(String[] args){
        RabbitReceiverServiceWithMultThread test = new RabbitReceiverServiceWithMultThread();
        test.init();
        test.testReceiveAndPublishWithMulThread();
    }

}
