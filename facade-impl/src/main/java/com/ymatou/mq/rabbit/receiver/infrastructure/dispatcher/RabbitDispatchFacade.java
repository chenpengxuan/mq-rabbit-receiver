package com.ymatou.mq.rabbit.receiver.infrastructure.dispatcher;

import com.ymatou.mq.rabbit.receiver.model.Message;
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
