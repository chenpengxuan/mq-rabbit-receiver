/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.ymatou.messagebus.facade.model.PublishMessageReq;

/**
 * @author luoshiqian 2017/3/30 11:18
 */
public class PublishMessageRestTest {


    private CloseableHttpClient buildClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(100);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();

        return HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setConnectionManager(cm).build();
    }

    @Test
    public void testPublish() throws Exception {
        PublishMessageReq req = new PublishMessageReq();

        req.setAppId("testdashen");
        req.setMsgUniqueId("123bcdf3");
        req.setBody("{\"orderId\":1321321}");
        req.setIp("127.0.0.1");
        req.setCode("publishok");

        CloseableHttpClient httpClient = buildClient();
        String url = "http://localhost:9310/api/publish";
        HttpPost httpPost = new HttpPost(url);

        StringEntity postEntity = new StringEntity(JSON.toJSONString(req), "UTF-8");
        httpPost.setEntity(postEntity);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");


        HttpResponse response = httpClient.execute(httpPost);

        String respBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println(respBody);
    }
}
