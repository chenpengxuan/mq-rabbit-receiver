/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest;

import com.ymatou.messagebus.facade.model.ReceiveMessageReq;

/**
 * @author luoshiqian 2016/8/31 14:12
 */
public interface ReceiveMessageResource {

    /**
     * 发布单条消息
     *
     * @param req
     * @return
     */
    RestResp publish(ReceiveMessageReq req);

    String fileStatus();


}
