/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.facade.aspect;

import com.ymatou.messagebus.facade.BaseRequest;
import com.ymatou.messagebus.facade.BaseResponse;
import com.ymatou.messagebus.facade.BizException;
import com.ymatou.messagebus.facade.ErrorCode;
import com.ymatou.mq.rabbit.receiver.util.Constants;
import com.ymatou.mq.rabbit.receiver.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Facade AOP.
 * <p>
 * 实现与业务无关的通用操作。
 * <p>
 * 1，日志
 * <p>
 * 2，异常处理等
 *
 * @author tuwenjie
 */
@Aspect
@Component
public class FacadeAspect {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(FacadeAspect.class);

    //@Pointcut("execution(* com.ymatou.mq.rabbit.receiver.facade.*Facade.*(*)) && args(req)")
    @Pointcut("execution(* com.ymatou.messagebus.facade.*Facade.*(*)) && args(req)")
    public void executeFacade(BaseRequest req) {
    }

    @Around("executeFacade(req)")
    public Object aroundFacadeExecution(ProceedingJoinPoint joinPoint, BaseRequest req)
            throws InstantiationException, IllegalAccessException {
        Logger logger = DEFAULT_LOGGER;

        if (req == null) {
            logger.error("{} Recv: null", joinPoint.getSignature());
            return buildErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, "request is null");
        }

        if (req.requireRequestId() && StringUtils.isEmpty(req.getRequestId())) {
            return buildErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, "requestId not provided");
        }

        if (req.requireAppId() && StringUtils.isEmpty(req.getAppId())) {
            return buildErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, "appId not provided");
        }

        long startTime = System.currentTimeMillis();

        if (StringUtils.isEmpty(req.getRequestId())) {
            req.setRequestId(Utils.uuid());
        }

        // log日志配有"logPrefix"占位符
        MDC.put(Constants.LOG_PREFIX, getRequestFlag(req));

        logger.debug("Recv:" + req);

        Object resp = null;

        try {
            req.validate();
            resp = joinPoint.proceed(new Object[]{req});
        } catch (IllegalArgumentException e) {
            //无效参数异常
            resp = buildErrorResponse(joinPoint, ErrorCode.ILLEGAL_ARGUMENT, e.getLocalizedMessage());
            logger.error("Invalid request: {}", req, e);
        }catch (BizException e) {
            //对于MQ业务，明确的业务异常都应该error报警
            resp = buildErrorResponse(joinPoint, ErrorCode.FAIL, e.getLocalizedMessage());
            logger.error("Biz error in executing request:{}", req, e);
        } catch (Throwable e) {
            //未知异常
            resp = buildErrorResponse(joinPoint, ErrorCode.UNKNOWN, e.getLocalizedMessage());
            logger.error("Unknown error in executing request:{}", req, e);
        } finally {
            logger.debug("Resp:" + resp);

            long consumedTime = System.currentTimeMillis() - startTime;
            if (consumedTime > 300) {
                logger.warn("slow query gt 300ms({}ms). Req:{}", consumedTime, req);
            }
            MDC.clear();
        }


        return resp;
    }


    private BaseResponse buildErrorResponse(ProceedingJoinPoint joinPoint, ErrorCode errorCode, String errorMsg)
            throws InstantiationException, IllegalAccessException {
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        BaseResponse resp = (BaseResponse) ms.getReturnType().newInstance();
        resp.setErrorCode(errorCode);
        resp.setErrorMessage(errorMsg);
        resp.setSuccess(false);
        return resp;

    }

    private String getRequestFlag(BaseRequest req) {
        return req.getClass().getSimpleName() + "|" + req.getRequestId();
    }

}
