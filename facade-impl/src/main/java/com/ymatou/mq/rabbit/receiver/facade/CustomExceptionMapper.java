/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.facade;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Enumeration;

/**
 * @author luoshiqian 2017/3/14 11:08
 */
public class CustomExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {

        HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        LOGGER.error("exception:{},clientIp:{},req:{},header:{}", new Object[] {exception.toString(), getIp(request),
                DynamicTraceInterceptor.getRequest(), getHeaders(request)});

        if (exception instanceof JsonMappingException || exception instanceof JsonParseException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).type("text/plain")
                    .build();
        } else if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).entity("Oops! the requested resource is not found!")
                    .type("text/plain").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal server error: " + exception.toString()).type(ContentType.TEXT_PLAIN_UTF_8)
                    .build();
        }

    }

    private String getHeaders(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer("");
        if (null != request && request.getHeaderNames() != null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                sb.append(header).append("=").append(JSON.toJSONString(request.getHeaders(header))).append("\n");
            }
        }
        return sb.toString();
    }

    private String getIp(HttpServletRequest request) {

        if (request == null) {
            return RpcContext.getContext().getRemoteHostName();
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
