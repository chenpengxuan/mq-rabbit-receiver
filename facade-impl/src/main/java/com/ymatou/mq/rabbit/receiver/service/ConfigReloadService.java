/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.service;

import javax.annotation.PostConstruct;

import com.alibaba.dubbo.common.utils.StringUtils;
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

import java.io.IOException;

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
        //启动时声明交换器、队列
        declareExchangeAndQueue();

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
        logger.info("declareExchangeAndQueue begin");
        //启动时声明交换器、队列
        declareExchangeAndQueue();
    }

    /**
     * 声明交换器、队列
     */
    void declareExchangeAndQueue(){
        for(AppConfig appConfig:MessageConfigService.appConfigMap.values()){
            String dispatchGroup = appConfig.getDispatchGroup();
            //若MQ类型为kafka则不声明
            Integer mqType = appConfig.getMqType();
            if(mqType == 1){
                continue;
            }
            for(QueueConfig queueConfig:appConfig.getMessageCfgList()){
                String exchange = String.format("%s_%s",appConfig.getAppId(),queueConfig.getCode());
                //声明exchange
                declareExchange(primaryChannel,exchange);
                declareExchange(secondaryChannel,exchange);
                for(CallbackConfig callbackConfig:queueConfig.getCallbackCfgList()){
                    String callbackKey = callbackConfig.getCallbackKey();
                    //声明queue
                    declareQueue(primaryChannel, exchange,callbackKey);
                    declareQueue(secondaryChannel, exchange,callbackKey);
                }
            }
        }
    }

    /**
     * 声明exchange
     * @param channel
     * @param exchange
     */
    void declareExchange(Channel channel, String exchange) {
        if (channel != null) {
            try {
                channel.exchangeDeclare(exchange, "topic", true);
            } catch (Exception e) {
                logger.error("declareExchange {} error", exchange, e);
            }
        }
    }

    /**
     * 声明队列
     * @param channel
     * @param exchange
     * @param callbackKey
     */
    void declareQueue(Channel channel, String exchange,String callbackKey) {
        if (channel != null) {
            try {
                channel.queueDeclare(callbackKey, true, false, false, null);
                channel.queueBind(callbackKey, exchange, getRouteKey(callbackKey));
            } catch (Exception e) {
                logger.error("declareQueue:{},{} error", exchange,callbackKey, e);
            }
        }
    }

    /**
     * 获取routeKey
     * @param callbackKey
     * @return
     */
    String getRouteKey(String callbackKey){
        String callbackNo = callbackKey.substring(callbackKey.lastIndexOf("_")+1,callbackKey.length());
        String routeKey = String.format("#.%s.#",callbackNo);
        logger.debug("routeKey:{},callbackKey:{}",routeKey,callbackKey);
        return routeKey;
    }

    /**
     * 删除交换器、队列
     */
    public void deleteExchangeAndQueueOfKafka(){
        try {
            for(AppConfig appConfig:MessageConfigService.appConfigMap.values()){
                Integer mqType = appConfig.getMqType();
                //若MQ类型为kafka则删除
                if(mqType == 1){
                    for(QueueConfig queueConfig:appConfig.getMessageCfgList()){
                        String exchange = String.format("%s_%s",appConfig.getAppId(),queueConfig.getCode());
                        logger.info("delete exchange:{}.",exchange);
                        //删除exchange
                        primaryChannel.exchangeDelete(exchange);
                        secondaryChannel.exchangeDelete(exchange);
                        for(CallbackConfig callbackConfig:queueConfig.getCallbackCfgList()){
                            String callbackKey = callbackConfig.getCallbackKey();
                            logger.info("delete queue:{}.",callbackKey);
                            //删除queue
                            primaryChannel.queueDelete(callbackKey);
                            secondaryChannel.queueDelete(callbackKey);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("delete exchange and queue error.",e);
        }
    }
}
