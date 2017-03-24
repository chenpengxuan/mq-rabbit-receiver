package com.ymatou.mq.rabbit.receiver.model;

import java.util.List;

/**
 * 队列配置模型
 * Created by zhangzhihua on 2017/3/24.
 */
public class QueueConfig {

    private String appId;

    private String bizCode;

    private String consumerId;

    private String callbackUrl;

    List<SubscribleConfig> subscribleConfigList;

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

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public List<SubscribleConfig> getSubscribleConfigList() {
        return subscribleConfigList;
    }

    public void setSubscribleConfigList(List<SubscribleConfig> subscribleConfigList) {
        this.subscribleConfigList = subscribleConfigList;
    }
}
