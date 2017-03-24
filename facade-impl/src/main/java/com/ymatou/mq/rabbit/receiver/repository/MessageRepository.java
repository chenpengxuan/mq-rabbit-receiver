package com.ymatou.mq.rabbit.receiver.repository;

import com.ymatou.mq.rabbit.receiver.model.Message;
import org.springframework.stereotype.Component;

/**
 * 消息数据操作
 * Created by zhangzhihua on 2017/3/23.
 */
@Component("messageRepository")
public class MessageRepository {

    public void save(Message msg){
        //TODO
    }
}
