package com.ymatou.messagebus.facade;

import com.ymatou.messagebus.facade.model.ReceiveMessageReq;
import com.ymatou.messagebus.facade.model.ReceiveMessageResp;

/**
 * 发布消息接口
 * 
 * @author wangxudong 2016年7月27日 下午7:00:13
 *
 */
public interface ReceiveMessageFacade {

    /**
     * 发布单条消息
     * 
     * @param req
     * @return
     */
    public ReceiveMessageResp publish(ReceiveMessageReq req);
}
