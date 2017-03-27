package com.ymatou.mq.rabbit.receiver.support;

import com.ymatou.mq.infrastructure.model.Message;

import java.util.List;

/**
 * fileDb操作类，本地模拟要替换为infra中的
 * Created by zhangzhihua on 2017/3/23.
 */
public class FileDb {

    /**
     * 保存消息
     * @param msg
     */
    public void saveMessage(Message msg){
        //TODO
    }

    /**
     * 提取messages
     * @return
     */
    public List<Message> takeMessages(){
        return null;
    }

    /**
     * 删除消息
     * @param message
     */
    public void deleteMessage(Message message){
        //TODO 删除消息
    }
}
