package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.support.FileDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地消息文件列表处理service
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("fileQueueProcessorService")
public class FileQueueProcessorService {

    @Autowired
    private FileDb fileDb;

    @Autowired
    private MessageService messageService;

    /**
     * 扫描队列并处理
     */
    public void takeAndProcess(){
        List<Message> messageList = this.takeMessageList();
        this.writeMessagesToMongo(messageList);
    }

    /**
     * 从本地文件队列读取一定量消息
     * @return
     */
    List<Message> takeMessageList(){
        return fileDb.takeMessages();
    }

    /**
     * 写消息到mongo
     */
    void writeMessagesToMongo(List<Message> messageList){
        if(CollectionUtils.isEmpty(messageList)){
            return;
        }
        for(Message message:messageList){
            //保存消息到mongo并删除本地队列记录 TODO 失败回滚
            messageService.saveMessage(message);
            fileDb.deleteMessage(message);
        }
    }


}
