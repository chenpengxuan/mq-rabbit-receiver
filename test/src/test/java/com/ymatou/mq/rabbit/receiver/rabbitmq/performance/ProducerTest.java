/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.performance;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.ymatou.mq.infrastructure.filedb.util.ThreadHelper;
import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.ymatou.mq.rabbit.receiver.rabbitmq.RabbitMqBaseTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author luoshiqian 2017/3/30 18:25
 */
public class ProducerTest extends RabbitMqBaseTest {

    int threadCount = 20;
    AtomicLong totalCount = new AtomicLong(0);

    @Test
    public void testOneConnection() throws Exception {
        Connection connection = newConnection();

        for(int i=0;i<threadCount;i++){

            new Thread(() -> {

                try {
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(trading_q, true, false, false, null);
                    while (true){
                        channel.basicPublish("", trading_q, MessageProperties.MINIMAL_PERSISTENT_BASIC, ("message_").getBytes());
                        totalCount.incrementAndGet();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }


        sleep(100000);

        System.out.println("totalCount="+totalCount.get());
        System.out.println("tps="+(totalCount.get()/60) +"/s" );
        System.exit(-1);

    }

    @Test
    public void testManyConnection() throws Exception {


        for(int i=0;i<threadCount;i++){
            new Thread(() -> {
                try {
                    Channel channel = newConnection().createChannel();
                    channel.queueDeclare(trading_q, true, false, false, null);
                    while (true){
                        channel.basicPublish("", trading_q, MessageProperties.MINIMAL_PERSISTENT_BASIC, ("message_").getBytes());
                        totalCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        }


        sleep(100000);

        System.out.println("totalCount="+totalCount.get());
        System.out.println("tps="+(totalCount.get()/60) +"/s" );
        System.exit(-1);

    }

}
