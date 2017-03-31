/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.service;

import javax.annotation.PostConstruct;

import com.google.common.collect.Maps;
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

import java.util.Map;

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

    private Map<String,Object> declareQueueArguments = Maps.newHashMap();

    @PostConstruct
    public void init() {
        messageConfigService.addConfigCacheListener(this);

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

        //queue 找master 策略，使用最小master的机器  详见：https://www.rabbitmq.com/ha.html  Queue Master Location
        declareQueueArguments.put("x-queue-master-locator","min-masters");
    }

    @Override
    public void callback() {
        MessageConfigService.appConfigMap.values().stream().flatMap(appConfig -> appConfig.getMessageCfgList().stream())
                .forEach(queue -> {
                    declareQueue(primaryChannel, queue.getCode());
                    declareQueue(secondaryChannel, queue.getCode());
                });
    }

    void declareQueue(Channel channel, String queue) {
        if (channel != null) {
            try {
                channel.queueDeclare(queue, true, false, false, declareQueueArguments);
            } catch (Exception e) {
                logger.error("declareQueue:{} error", queue, e);
            }
        }
    }
}
