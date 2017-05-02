/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author luoshiqian 2017/3/13 15:52
 */
public class RabbitMqBaseTest {

    public static final String trading_ex = "ex_trading";
    public static final String trading_q = "q_trading";
    public static final String paygateway_ex = "ex_paygateway";
    public static final String paygateway_q = "q_paygateway";

    // public static final String routing_key_1

    public ConnectionFactory factory;
    public List<Address> addressList = Lists.newArrayList(
            new Address("172.16.103.127", 5672),
            new Address("172.16.103.128", 5672),
            new Address("172.16.103.129", 5672)
    );

    @Before
    public void before() {
        factory = new ConnectionFactory();
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");

        factory.setAutomaticRecoveryEnabled(true);
        factory.setHeartbeatExecutor(ScheduledExecutorHelper.newScheduledThreadPool(3, "rabbitmq-heartbeat-thread"));
    }

    public Connection newConnection() throws IOException, TimeoutException {
        return factory.newConnection(addressList);
    }

    public void hold() throws Exception {
        System.in.read();
    }

    public void sleep(int time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
