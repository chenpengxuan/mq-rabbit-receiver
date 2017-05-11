/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.rabbit.receiver.facade;

import org.springframework.util.StreamUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 记录请求request body 完整信息
 * @author luoshiqian
 */
@Priority(Priorities.USER)
public class DynamicTraceInterceptor implements ReaderInterceptor, WriterInterceptor {

    private static final ThreadLocal<String> reqThreadLocal = new ThreadLocal<>();

    public static String getRequest(){
        return reqThreadLocal.get();
    }

    public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException, WebApplicationException {
        String requestStr = StreamUtils.copyToString(readerInterceptorContext.getInputStream(), Charset.forName("utf-8"));
        reqThreadLocal.set(requestStr);
        readerInterceptorContext.setInputStream(new ByteArrayInputStream(requestStr.getBytes(Charset.forName("utf-8"))));
        return readerInterceptorContext.proceed();
    }

    public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
        reqThreadLocal.remove();
        writerInterceptorContext.proceed();
    }
}
