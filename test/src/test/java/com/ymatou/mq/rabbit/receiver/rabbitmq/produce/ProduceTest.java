/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rabbitmq.produce;

import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;

import com.ymatou.mq.rabbit.receiver.rabbitmq.ExchangeQueueBaseTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author luoshiqian 2017/3/13 18:18
 */
public class ProduceTest extends ExchangeQueueBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProduceTest.class);

    @Test
    public void testProduce() {


    }

    @Test
    public void testProduceConfirm() throws Exception {
        producerChannel.confirmSelect();

        final SortedSet<Long> unconfirmedSet =
                Collections.synchronizedSortedSet(new TreeSet<Long>());

//        producerChannel.addConfirmListener(new ConfirmListener() {
//            @Override
//            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
////                LOGGER.info("confirm send ok {},{}",deliveryTag,multiple);
//                if (multiple) {
//                    unconfirmedSet.headSet(deliveryTag + 1).clear();
//                } else {
//                    unconfirmedSet.remove(deliveryTag);
//                }
//            }
//
//            @Override
//            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
//                LOGGER.info("confirm send handle nack {},{}",deliveryTag,multiple);
//            }
//        });


        int messageCount = 10000000;
        int sendCount = 0;
        while (messageCount > 0){
//            long nextSeqNo = producerChannel.getNextPublishSeqNo();
////            LOGGER.info("=================================getNextPublishSeqNo:{}",nextSeqNo);
//            unconfirmedSet.add(nextSeqNo);
//            messageCount--;
            long start = System.currentTimeMillis();
            try {
                producerChannel.basicPublish("", trading_q, MessageProperties.MINIMAL_PERSISTENT_BASIC, ("message_" + messageCount).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            long duration = System.currentTimeMillis() - start;

            if(duration > 300){
                LOGGER.info("slow publish :{}",duration);
            }

//            TimeUnit.SECONDS.sleep(1);
            sendCount++;
        }

        hold();

//        producerChannel.waitForConfirmsOrDie();

        LOGGER.info("send:{},unconfirmedSetSize:{}",sendCount,unconfirmedSet.size());

    }




}
