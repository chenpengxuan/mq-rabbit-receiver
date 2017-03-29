/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest.impl;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.mq.rabbit.receiver.rest.PublishMessageResource;
import com.ymatou.mq.rabbit.receiver.util.Constants;


@Component("publishMessageResource")
@Produces({"application/json; charset=UTF-8"})
@Service(protocol = "rest")
@Path("/{api:(?i:api)}")
public class PublishMessageResourceImpl implements PublishMessageResource {

    public static final Logger logger = LoggerFactory.getLogger(PublishMessageResourceImpl.class);

    @POST
    @Path("/{shutdown:(?i:shutdown)}")
    @Override
    public String shutdown() {
        Constants.ctx.close();
        return "";
    }
}
