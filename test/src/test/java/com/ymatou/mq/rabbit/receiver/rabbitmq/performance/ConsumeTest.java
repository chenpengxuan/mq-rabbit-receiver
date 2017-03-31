/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.performance;


import com.ymatou.mq.rabbit.receiver.rabbitmq.ExchangeQueueBaseTest;
import com.ymatou.mq.rabbit.receiver.rabbitmq.consume.TestConsumer;
import org.junit.Test;

/**
 * @author luoshiqian 2017/3/13 18:18
 */
public class ConsumeTest extends ExchangeQueueBaseTest {

    @Test
    public void testConsume()throws Exception{

        consuerChannel.basicConsume(trading_q,false, new TestConsumer(consuerChannel, trading_ex, trading_q));

        hold();
    }


    public void testConsumeConfirm(){

    }
}
