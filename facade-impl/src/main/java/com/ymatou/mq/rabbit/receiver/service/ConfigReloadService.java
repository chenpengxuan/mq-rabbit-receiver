/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.service;

import javax.annotation.PostConstruct;

import com.ymatou.mq.infrastructure.model.AppConfig;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.infrastructure.support.ConfigReloadListener;
import com.ymatou.mq.rabbit.RabbitConnectionFactory;
import com.ymatou.mq.rabbit.config.RabbitConfig;
import com.ymatou.mq.rabbit.support.RabbitConstants;

/**
 * 定时刷新配置 回调处理
 * 
 * @author luoshiqian 2017/3/31 10:49
 */
@Component
public class ConfigReloadService implements ConfigReloadListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigReloadService.class);

    @Autowired
    private MessageConfigService messageConfigService;

    @Autowired
    private RabbitConfig rabbitConfig;

    private Connection primaryConnection;

    private Connection secondaryConnection;

    private Channel primaryChannel;

    private Channel secondaryChannel;


    @PostConstruct
    public void init() {
        messageConfigService.addConfigCacheListener(this);
        //启动时声明队列
        handleDeclareQueue();

        try {
            primaryConnection = RabbitConnectionFactory.createConnection(RabbitConstants.CLUSTER_MASTER, rabbitConfig);
            primaryChannel = primaryConnection.createChannel();
        } catch (Exception e) {
            logger.error("create primaryConnection/primaryChannel error", e);
        }
        try {
            secondaryConnection = RabbitConnectionFactory.createConnection(RabbitConstants.CLUSTER_SLAVE, rabbitConfig);
            secondaryChannel = secondaryConnection.createChannel();
        } catch (Exception e) {
            logger.error("create secondaryConnection/secondaryChannel error", e);
        }
    }

    @Override
    public void callback() {
        //声明队列处理
        handleDeclareQueue();
    }

    /**
     * 声明队列处理
     */
    void handleDeclareQueue(){
        for(AppConfig appConfig:MessageConfigService.appConfigMap.values()){
            for(QueueConfig queueConfig:appConfig.getMessageCfgList()){
                for(CallbackConfig callbackConfig:queueConfig.getCallbackCfgList()){
                    String exchange = String.format("%s_%s",appConfig.getAppId(),queueConfig.getCode());
                    String queue = callbackConfig.getCallbackKey();
                    declareQueue(primaryChannel, exchange,queue);
                    declareQueue(secondaryChannel, exchange,queue);
                }
            }
        }
    }

    /**
     * 声明队列
     * @param channel
     * @param exchange
     * @param queue
     */
    void declareQueue(Channel channel, String exchange,String queue) {
        if (channel != null) {
            try {
                channel.exchangeDeclare(exchange, "fanout", true);
                channel.queueDeclare(queue, true, false, false, null);
                channel.queueBind(queue, exchange, queue);
            } catch (Exception e) {
                logger.error("declareQueue:{},{} error", exchange,queue, e);
            }
        }
    }
}
