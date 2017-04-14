/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.ymatou.mq.rabbit.support.RabbitConstants;
import org.springframework.stereotype.Component;

/**
 * @author luoshiqian 2017/3/27 16:40
 */
@Component
@DisconfFile(fileName = "receiver.properties")
public class ReceiverConfig {

    /**
     * 是否开启master集群，默认开启
     */
    private boolean masterEnable = true;

    /**
     * 是否开启slave集群，默认开启
     */
    private boolean slaveEnable = true;

    @DisconfFileItem(name = "rabbitmq.master.enable")
    public boolean isMasterEnable() {
        return masterEnable;
    }

    public void setMasterEnable(boolean masterEnable) {
        this.masterEnable = masterEnable;
    }

    @DisconfFileItem(name = "rabbitmq.slave.enable")
    public boolean isSlaveEnable() {
        return slaveEnable;
    }

    public void setSlaveEnable(boolean slaveEnable) {
        this.slaveEnable = slaveEnable;
    }

    /**
     * 获取当前集群
     * @return
     */
    public String getCurrentCluster(){
        if(masterEnable){
            return RabbitConstants.CLUSTER_MASTER;
        }else if(slaveEnable){
            return RabbitConstants.CLUSTER_SLAVE;
        }else{
            return null;
        }
    }
}
