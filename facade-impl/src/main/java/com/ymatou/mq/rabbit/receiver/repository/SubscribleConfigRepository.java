package com.ymatou.mq.rabbit.receiver.repository;

import com.ymatou.mq.rabbit.receiver.model.SubscribleConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息订阅配置数据操作
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("subscribleConfigRepository")
public class SubscribleConfigRepository {

    /**
     * 根据应用id、业务code查找消费者id列表
     * @param appId
     * @param bizCode
     * @return
     */
    public List<SubscribleConfig> getSubscribleConfigList(String appId, String bizCode){
        return null;
    }
}
