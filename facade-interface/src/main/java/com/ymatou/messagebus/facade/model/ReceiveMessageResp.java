/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.facade.model;

import com.ymatou.messagebus.facade.BaseResponse;

/**
 * 发布消息响应
 * 
 * @author wangxudong 2016年7月27日 下午6:58:14
 *
 */
public class ReceiveMessageResp extends BaseResponse {

    private static final long serialVersionUID = -6242698339120920406L;

    private String uuid;

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
