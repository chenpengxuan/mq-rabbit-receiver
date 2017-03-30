/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.consume;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author luoshiqian 2017/3/15 11:32
 */
public class TestConsumer implements Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestConsumer.class);

    private Channel channel;
    private String exchange;
    private String queue;

    public TestConsumer(Channel channel, String exchange, String queue) {
        this.channel = channel;
        this.exchange = exchange;
        this.queue = queue;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOGGER.info("queue:{} handle consume ok  registerd,consumerTag:{}",queue,consumerTag);
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        LOGGER.info("queue:{} handle cancel ok,consumerTag:{}",queue,consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOGGER.info("queue:{} handle cancel,consumerTag:{}",queue,consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOGGER.info("queue:{} handleShutdownSignal ok consumerTag:{}",queue,consumerTag);
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        LOGGER.info("queue:{} handleRecoverOk ok consumerTag:{}",queue,consumerTag);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        long deliveryTag = envelope.getDeliveryTag();

        LOGGER.info("deliveryTag:{},queue:{}Delivery:{},consumerTag:{}",
                deliveryTag,queue,new String(body, Charset.forName("utf-8")),consumerTag);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel.basicAck(deliveryTag, false);
    }
}
