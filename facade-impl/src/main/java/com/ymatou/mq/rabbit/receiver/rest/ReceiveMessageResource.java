/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest;

import com.ymatou.messagebus.facade.model.ReceiveMessageRestReq;

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
    RestResp publish(ReceiveMessageRestReq req);

    String fileStatus();

    /**
     * 删除kafka类型的队列声明
     * @return
     */
    String deleteKafkaQueue();


}
