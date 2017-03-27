package com.ymatou.mq.rabbit.receiver.support;

import com.ymatou.mq.infrastructure.model.Message;
import org.springframework.stereotype.Component;

/**
 * rabbit分发facade，本地模拟
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("rabbitDispatchFacade")
public class RabbitDispatchFacade {

    public void dispatchMessage(Message msg){
        //TODO

    }
}
