package com.ymatou.mq.rabbit.receiver.service;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.ymatou.mq.infrastructure.service.MessageService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.mq.infrastructure.filedb.FileDb;
import com.ymatou.mq.infrastructure.filedb.FileDbConfig;
import com.ymatou.mq.infrastructure.filedb.PutExceptionHandler;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.rabbit.receiver.config.FileDbConf;

/**
 * 本地消息文件列表处理service Created by zhangzhihua on 2017/3/24.
 */
@Component
public class FileQueueProcessorService implements Function<Pair<String, String>, Boolean>, PutExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileQueueProcessorService.class);

    private FileDb fileDb;
    @Autowired
    private FileDbConf fileDbConf;

    @Autowired
    private MessageService messageService;

    public FileDb getFileDb() {
        return fileDb;
    }

    public void setFileDb(FileDb fileDb) {
        this.fileDb = fileDb;
    }

    @PostConstruct
    public void init() {
        FileDbConfig fileDbConfig = FileDbConfig.newInstance()
                .setDbName(fileDbConf.getDbName())
                .setDbPath(fileDbConf.getDbPath())
                .setConsumerThreadNums(fileDbConf.getConsumerThreadNums())
                .setConsumeDuration(fileDbConf.getConsumeDuration())
                .setMaxConsumeSizeInDuration(fileDbConf.getMaxConsumeSizeInDuration())
                .setConsumer(this)
                .setPutExceptionHandler(this);

        fileDb = FileDb.newFileDb(fileDbConfig);
    }


    /**
     * 保存成文件队列
     * 
     * @param message
     */
    public void saveMessageToFileDb(Message message) {
        fileDb.put(message.getId(), Message.toJsonString(message));
    }

    /**
     * 消费从文件获取到的数据 入库成功 返回true
     * 
     * @param pair
     * @return
     */
    @Override
    public Boolean apply(Pair<String, String> pair) {
        Boolean success = Boolean.FALSE;
        try {
            Message message = Message.fromJson(pair.getValue());
            success = messageService.saveMessage(message);
        } catch (Exception e) {
            logger.error("save message to mongo error", e);
        }
        return success;
    }

    /**
     * 异常处理
     * 
     * @param key
     * @param value
     * @param throwable
     */
    @Override
    public void handleException(String key, String value, Optional<Throwable> throwable) {

        logger.warn("key:{},value:{} can not save to filedb ", key, value,
                throwable.isPresent() ? throwable.get() : "");

        Message message = Message.fromJson(value);

        try {
            messageService.saveMessage(message);
        } catch (Exception e) {
            logger.error("filedb handleException save message to mongo error", e);
        }
    }

    @PreDestroy
    public void destroy(){
        fileDb.close();
    }
}
