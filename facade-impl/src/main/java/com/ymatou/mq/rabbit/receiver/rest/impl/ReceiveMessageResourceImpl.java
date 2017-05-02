/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.container.page.pages.SystemPageHandler;
import com.alibaba.fastjson.JSON;
import com.ymatou.messagebus.facade.ReceiveMessageFacade;
import com.ymatou.messagebus.facade.model.ReceiveMessageReq;
import com.ymatou.messagebus.facade.model.ReceiveMessageResp;
import com.ymatou.mq.rabbit.receiver.facade.aspect.FacadeAspect;
import com.ymatou.mq.rabbit.receiver.rest.ReceiveMessageResource;
import com.ymatou.mq.rabbit.receiver.rest.RestResp;
import com.ymatou.mq.rabbit.receiver.service.ConfigReloadService;
import com.ymatou.mq.rabbit.receiver.service.MessageFileQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Component("publishMessageResource")
@Produces({"application/json; charset=UTF-8"})
@Service(protocol = "rest")
@Path("/{message:(?i:message)}")
public class ReceiveMessageResourceImpl implements ReceiveMessageResource {

    public static final Logger logger = LoggerFactory.getLogger(ReceiveMessageResourceImpl.class);

    @Resource
    ReceiveMessageFacade receiveMessageFacade;

    @Autowired
    MessageFileQueueService messageFileQueueService;

    @Autowired
    ConfigReloadService configReloadService;

    @Override
    @POST
    @Path("/{publish:(?i:publish)}")
    public RestResp publish(ReceiveMessageReq req) {
        ReceiveMessageResp receiveMessageResp = receiveMessageFacade.publish(req);
        return RestResp.newInstance(receiveMessageResp);
    }

    @GET
    @Path("/{filestatus:(?i:filestatus)}")
    @Override
    public String fileStatus() {
        return messageFileQueueService.getFileDb().status();
    }

    @GET
    @Path("/{deleteKafkaQueue:(?i:deleteKafkaQueue)}")
    @Override
    public String deleteKafkaQueue() {
        configReloadService.deleteExchangeAndQueueOfKafka();
        return "ok";
    }
}
