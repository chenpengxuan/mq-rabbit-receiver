package com.ymatou.mq.rabbit.receiver.model;

/**
 * 消息分发明细模型
 * Created by zhangzhihua on 2017/3/24.
 */
public class MessageDispatchDetail {

    /**
     * 应用id
     */
    private String appId;

    /**
     * 业务code
     */
    private String bizCode;

    /**
     * 消息id,客户端消息标识
     */
    private String msgId;

    /**
     * 消息uuid，服务端标识
     */
    private String msgUuid;

    /**
     * 订阅者id
     */
    private String consumerId;

    /**
     * 消息分发状态
     */
    private int status;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgUuid() {
        return msgUuid;
    }

    public void setMsgUuid(String msgUuid) {
        this.msgUuid = msgUuid;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
