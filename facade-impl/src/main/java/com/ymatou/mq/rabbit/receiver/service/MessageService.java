package com.ymatou.mq.rabbit.receiver.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.repository.MessageDispatchDetailRepository;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import com.ymatou.mq.infrastructure.service.MessageConfigService;

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
     * 
     * @param msg
     */
    public boolean saveMessage(Message msg) {
        // 保存消息
        if (messageRepository.save(msg)) {
            // 获取消息分发明细列表
            List<MessageDispatchDetail> detailList = this.buildMessageDispatchDetailList(msg);
            // 保存分发明细列表
            if (CollectionUtils.isNotEmpty(detailList)) {
                for (MessageDispatchDetail detail : detailList) {
                    if (!messageDispatchDetailRepository.saveDetail(detail)) {
                        // 有一条没保存成功 直接退出 等待下回再次保存
                        return false;
                    }
                }
            }
            // 所有保存成功 ，返回true
            return true;
        }
        return false;
    }

    /**
     * 根据配置构造所有要分发的明细列表
     * @param msg
     * @return
     */
    List<MessageDispatchDetail> buildMessageDispatchDetailList(Message msg){
        List<MessageDispatchDetail> detailList = new ArrayList<MessageDispatchDetail>();
        List<CallbackConfig> callbackConfigList =  messageConfigService.getCallbackConfigList(msg.getAppId(),msg.getQueueCode());
        if(CollectionUtils.isNotEmpty(callbackConfigList)){
            for(CallbackConfig callbackConfig:callbackConfigList){
                MessageDispatchDetail detail = new MessageDispatchDetail();
                detail.setId(this.buildDetailId(msg,callbackConfig));
                detail.setMsgId(msg.getId());
                detail.setBizId(msg.getBizId());
                detail.setAppId(msg.getAppId());
                detail.setQueueCode(msg.getQueueCode());
                detail.setConsumerId(callbackConfig.getCallbackKey());
                detail.setCreateTime(new Date());
            }
        }
        return detailList;
    }

    /**
     * 生成明细id
     * @param msg
     * @param callbackConfig
     * @return
     */
    String buildDetailId(Message msg,CallbackConfig callbackConfig){
        return String.format("%s_%s",msg.getId(),callbackConfig.getCallbackKey());
    }
}
