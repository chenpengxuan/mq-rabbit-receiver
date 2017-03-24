package com.ymatou.mq.rabbit.receiver.repository;

import com.ymatou.mq.rabbit.receiver.model.QueueConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * queue配置数据操作
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("queueConfigRepository")
public class QueueConfigRepository {

    /**
     * 根据appId查找队列配置列表
     * @param appId
     * @return
     */
    public List<QueueConfig> getQueueConfigList(String appId){
        return null;
    }
}
