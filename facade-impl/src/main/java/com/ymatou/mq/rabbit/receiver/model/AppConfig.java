package com.ymatou.mq.rabbit.receiver.model;

import java.util.List;

/**
 * 应用配置模型
 * Created by zhangzhihua on 2017/3/24.
 */
public class AppConfig {

    /**
     * appid
     */
    private String appId;

    /**
     * 队列配置列表
     */
    private List<QueueConfig> queueConfigList;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<QueueConfig> getQueueConfigList() {
        return queueConfigList;
    }

    public void setQueueConfigList(List<QueueConfig> queueConfigList) {
        this.queueConfigList = queueConfigList;
    }
}
