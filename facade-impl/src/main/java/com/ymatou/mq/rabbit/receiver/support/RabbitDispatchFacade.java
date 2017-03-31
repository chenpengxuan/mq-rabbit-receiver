package com.ymatou.mq.rabbit.receiver.support;

import com.ymatou.mq.infrastructure.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * rabbit分发facade，本地模拟
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("rabbitDispatchFacade")
public class RabbitDispatchFacade {

    private static final Logger logger = LoggerFactory.getLogger(RabbitDispatchFacade.class);

    public void dispatchMessage(Message msg){
        logger.info("direct dispatch msg:{}",msg);
    }
}
