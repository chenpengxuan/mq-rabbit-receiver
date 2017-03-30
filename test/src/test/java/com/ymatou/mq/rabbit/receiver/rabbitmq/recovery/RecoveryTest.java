/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.recovery;


import com.ymatou.mq.rabbit.receiver.rabbitmq.ExchangeQueueBaseTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luoshiqian 2017/3/13 16:00
 */
public class RecoveryTest extends ExchangeQueueBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryTest.class);


    /**
     * create one conn ,two channel
     * 
     * @throws Exception
     */
    @Test
    public void testRecovery() throws Exception {

        // 手动关闭 ，看日志
        hold();
    }

}
