package com.ymatou.mq.rabbit.receiver.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.repository.MessageDispatchDetailRepository;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("messageService")
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageDispatchDetailRepository messageDispatchDetailRepository;

    @Autowired
    private MessageConfigService messageConfigService;

    /**
     * 保存消息及分发明细
     * @param msg
     */
    public void saveMessage(Message msg){
        //保存消息
        messageRepository.save(msg);
        //获取消息分发明细列表
        List<MessageDispatchDetail> detailList = this.buildMessageDispatchDetailList(msg);
        //保存分发明细列表 TODO 一致性处理
        if(CollectionUtils.isNotEmpty(detailList)){
            for(MessageDispatchDetail detail:detailList){
                messageDispatchDetailRepository.saveDetail(detail);
            }
        }
    }

    /**
     * 根据配置构造所有要分发的明细列表
     * @param msg
     * @return
     */
    List<MessageDispatchDetail> buildMessageDispatchDetailList(Message msg){
        List<MessageDispatchDetail> detailList = new ArrayList<MessageDispatchDetail>();
        List<CallbackConfig> subscribleConfigList =  messageConfigService.getSubscribleConfigList(msg.getAppId(),msg.getBizCode());
        if(CollectionUtils.isNotEmpty(subscribleConfigList)){
            for(CallbackConfig subscribleConfig:subscribleConfigList){
                MessageDispatchDetail detail = new MessageDispatchDetail();
                //TODO
            }
        }
        return detailList;
    }
}
