package com.ymatou.mq.rabbit.receiver.repository;

import com.ymatou.mq.rabbit.receiver.model.MessageDispatchDetail;
import org.springframework.stereotype.Component;

/**
 * 消息分发明细数据操作
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("messageDispatchDetailRepository")
public class MessageDispatchDetailRepository {

    /**
     * 保存分发明细
     * @param detail
     */
    public void saveDetail(MessageDispatchDetail detail){
        //TODO
    }
}
