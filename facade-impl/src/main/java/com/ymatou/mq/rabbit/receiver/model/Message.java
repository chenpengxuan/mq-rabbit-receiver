package com.ymatou.mq.rabbit.receiver.model;

/**
 * 消息模型
 * Created by zhangzhihua on 2017/3/23.
 */
public class Message {

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
     * 消息体
     */
    private String body;

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
}
