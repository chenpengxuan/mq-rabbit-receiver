/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luoshiqian 2017/3/13 16:37
 */
public class ExchangeQueueBaseTest extends RabbitMqBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeQueueBaseTest.class);

    protected Channel producerChannel;
    protected Channel consuerChannel;


    @Before
    public void setUp() throws Exception {
        AutorecoveringConnection connection = (AutorecoveringConnection) newConnection();
        LOGGER.info("new connection:{}", connection);
        connection.addRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                LOGGER.info("connection:{},recovered", connection);
            }

            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                LOGGER.info("connection:{},started", connection);
            }
        });
        producerChannel = initProducerChannel(connection);
        consuerChannel = initConsumerChannel(connection);
    }

    public Channel initProducerChannel(Connection connection) throws Exception {
        Channel channel = connection.createChannel();

//        declareExchangeAndQueue(channel, trading_ex, trading_q);
//        declareExchangeAndQueue(channel, paygateway_ex, paygateway_q);

        return channel;
    }

    public Channel initConsumerChannel(Connection connection) throws Exception {
        Channel channel = connection.createChannel();

        declareExchangeAndQueue(channel, trading_ex, trading_q);
        bindExchangeForConsumer(channel, trading_ex, trading_q);

        declareExchangeAndQueue(channel, paygateway_ex, paygateway_q);
        bindExchangeForConsumer(channel, paygateway_ex, paygateway_q);

        return channel;
    }

    public void declareExchangeAndQueue(Channel channel, String exchange, String queue) throws Exception {
//        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queue, true, false, false, null);
    }

    public void bindExchangeForConsumer(Channel channel, String exchange, String queue) throws Exception {
//        channel.queueBind(queue, exchange, queue);
        channel.basicQos(1);
    }


}
