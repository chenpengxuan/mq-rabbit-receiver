package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.ymatou.mq.rabbit.receiver.model.AppConfig;
import com.ymatou.mq.rabbit.receiver.model.QueueConfig;
import com.ymatou.mq.rabbit.receiver.model.SubscribleConfig;
import com.ymatou.mq.rabbit.receiver.repository.AppConfigRepository;
import com.ymatou.mq.rabbit.receiver.repository.QueueConfigRepository;
import com.ymatou.mq.rabbit.receiver.repository.SubscribleConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息配置服务
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("messageConfigService")
public class MessageConfigService {

    private static final Logger logger = LoggerFactory.getLogger(MessageConfigService.class);

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private QueueConfigRepository queueConfigRepository;

    @Autowired
    private SubscribleConfigRepository subscribleConfigRepository;

    /**
     * 应用配置信息
     */
    private List<AppConfig> appConfigList = new ArrayList<AppConfig>();

    /**
     * 装载配置
     */
    public void loadConfig(){
        //TODO 优化加载操作
        //get app list
        List<AppConfig> newAppConfigList = appConfigRepository.getAllAppConfig();
        if(CollectionUtils.isNotEmpty(newAppConfigList)){
            for(AppConfig appConfig:newAppConfigList){
                //get queue list
                List<QueueConfig> queueConfigList = queueConfigRepository.getQueueConfigList(appConfig.getAppId());
                if(CollectionUtils.isNotEmpty(queueConfigList)){
                    appConfig.setQueueConfigList(queueConfigList);
                    for(QueueConfig queueConfig:queueConfigList){
                        //get subscrible list
                        List<SubscribleConfig> subscribleConfigList = subscribleConfigRepository.getSubscribleConfigList(queueConfig.getAppId(),queueConfig.getBizCode());
                        if(CollectionUtils.isNotEmpty(subscribleConfigList)){
                            queueConfig.setSubscribleConfigList(subscribleConfigList);
                        }
                    }
                }
            }
        }
        //TODO 基于事件监听处理
        //更新配置
        this.appConfigList = newAppConfigList;
    }

    /**
     * 若配置未加载则加载配置
     */
    void loadConfigIfNotLoaded(){
        if(appConfigList == null || appConfigList.size() == 0){
            this.loadConfig();
        }
    }

    /**
     * 根据appId查找app配置
     * @param appId
     * @return
     */
    public AppConfig getAppConfig(String appId){
        //TODO 从本地缓存找
        loadConfigIfNotLoaded();
        return null;
    }

    /**
     * 根据appId,bizCode查找队列配置
     * @param appId
     * @param bizCode
     * @return
     */
    public QueueConfig getQueueConfig(String appId,String bizCode){
        //TODO 从本地缓存找
        loadConfigIfNotLoaded();
        return null;
    }

    /**
     * 根据appId，bizCode获取订阅者配置列表
     * @param appId
     * @param bizCode
     * @return
     */
    public List<SubscribleConfig> getSubscribleConfigList(String appId, String bizCode){
        //TODO 从本地缓存找
        loadConfigIfNotLoaded();
        return null;
    }
}
