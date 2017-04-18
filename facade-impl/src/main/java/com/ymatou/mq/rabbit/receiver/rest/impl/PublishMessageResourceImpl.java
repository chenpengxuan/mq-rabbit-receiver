/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.messagebus.facade.PublishMessageFacade;
import com.ymatou.messagebus.facade.model.PublishMessageReq;
import com.ymatou.messagebus.facade.model.PublishMessageResp;
import com.ymatou.mq.rabbit.receiver.rest.PublishMessageResource;
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


@Component("publishMessageResource")
@Produces({"application/json; charset=UTF-8"})
@Service(protocol = "rest")
@Path("/{api:(?i:api)}")
public class PublishMessageResourceImpl implements PublishMessageResource {

    public static final Logger logger = LoggerFactory.getLogger(PublishMessageResourceImpl.class);

    //private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Resource
    PublishMessageFacade publishMessageFacade;

    @Autowired
    MessageFileQueueService messageFileQueueService;

    @Override
    @POST
    @Path("/{publish:(?i:publish)}")
    public PublishMessageResp publish(PublishMessageReq req) {
        /*
        PublishMessageReq request = new PublishMessageReq();
        request.setAppId(req.getAppId());
        request.setCode(req.getCode());
        request.setIp(req.getIp());
        request.setMsgUniqueId(req.getMsgUniqueId());
        request.setBody(JSON.toJSONStringWithDateFormat(req.getBody(), DATE_FORMAT));
        */
        return publishMessageFacade.publish(req);
    }

    @GET
    @Path("/{filestatus:(?i:filestatus)}")
    @Override
    public String fileStatus() {
        return messageFileQueueService.getFileDb().status();
    }
}
