/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.rabbit.receiver.util;

import com.google.common.base.Optional;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static volatile String localIp;

    private Utils() {};

    public static String uuid() {
        return new ObjectId().toHexString();
    }

    public static void main(String[] args) {
        System.out.println(uuid());

        System.out.println(DateFormatUtils.format(new ObjectId("58d8bd4a07d5dbf71cb88e20").getDate(),"yyyyMM"));
        System.out.println(new ObjectId("58d8bd4a07d5dbf71cb88e20").getDate());
    }

    public static String localIp() {
        if (localIp != null) {
            return localIp;
        }
        synchronized (Utils.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    localIp = "127.0.0.1";
                    LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead. {}", t.getMessage(), t);
                }
            }

            if (localIp == null) {
                localIp = "127.0.0.1";
                LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead");
            }

            return localIp;
        }
    }

    public static String readVersion() {
        try {
            Resource resource = new ClassPathResource("version.txt");
            return StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("Failed to read version", e);
            return "Failed to read version:" + e.getMessage();
        }
    }

    /**
     * 处理null问题
     *
     * @return
     */
    public static BigDecimal zeroIfNull(BigDecimal number) {
        BigDecimal zero = BigDecimal.ZERO;
        return optional(number, zero);
    }

    private static <T> T optional(T t, T defaultValue) {
        return Optional.fromNullable(t).or(defaultValue);
    }
}
