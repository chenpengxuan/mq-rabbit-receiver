/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq;

import com.google.common.collect.Lists;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.ymatou.mq.infrastructure.support.ScheduledExecutorHelper;
import org.junit.Before;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author luoshiqian 2017/3/13 15:52
 */
public class RabbitMqBaseTest {

    public static final String trading_ex = "ex_trading";
    public static final String trading_q = "q1_trading";
    public static final String paygateway_ex = "ex_paygateway";
    public static final String paygateway_q = "q_paygateway";

    // public static final String routing_key_1

    public ConnectionFactory factory;
    public List<Address> addressList = Lists.newArrayList(
            new Address("172.16.101.19", 5672));

    @Before
    public void before() {
        factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("javamqv2");

        factory.setAutomaticRecoveryEnabled(true);
        factory.setHeartbeatExecutor(ScheduledExecutorHelper.newScheduledThreadPool(3, "rabbitmq-heartbeat-thread"));
    }

    public Connection newConnection() throws IOException, TimeoutException {
        return factory.newConnection(addressList);
    }

    public void hold() throws Exception {
        System.in.read();
    }

}
