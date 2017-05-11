/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.consume;


import com.ymatou.mq.rabbit.receiver.rabbitmq.ExchangeQueueBaseTest;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author luoshiqian 2017/3/13 18:18
 */
public class ConsumeTest extends ExchangeQueueBaseTest {

    @Test
    public void testConsume()throws Exception{

        consuerChannel.basicConsume(trading_q,false, new TestConsumer(consuerChannel, trading_ex, trading_q));


        TimeUnit.SECONDS.sleep(10);
        consuerChannel.basicQos(20);//无效
        hold();
    }


    public void testConsumeConfirm(){

    }
}
