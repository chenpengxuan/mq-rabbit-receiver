package com.ymatou.mq.rabbit.receiver.repository;

import com.ymatou.mq.rabbit.receiver.model.AppConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * app配置数据操作
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("appConfigRepository")
public class AppConfigRepository {

    /**
     * 获取所有app配置列表
     * @return
     */
    public List<AppConfig> getAllAppConfig(){
        return null;
    }
}
