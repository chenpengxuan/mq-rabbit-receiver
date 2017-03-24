package com.ymatou.mq.rabbit.receiver.task;

import com.ymatou.mq.rabbit.receiver.service.FileQueueProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本地消息文件列队处理线程
 * Created by zhangzhihua on 2017/3/24.
 */
public class FileQueueProcessorThread implements  Runnable{

    private static final Logger logger = LoggerFactory.getLogger(FileQueueProcessorThread.class);

    //TODO 线程注入还是其它方式?
    private FileQueueProcessorService fileQueueProcessorService;

    @Override
    public void run() {
        while (true){

            //sync处理
            try {
                fileQueueProcessorService.takeAndProcess();
            } catch (Exception e) {
                logger.error("file queue process error:" + e);
            }

            //sleep
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("thread occur error:" + e);
            }
        }
    }
}
