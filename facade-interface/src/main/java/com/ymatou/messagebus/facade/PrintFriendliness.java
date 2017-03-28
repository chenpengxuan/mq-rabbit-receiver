package com.ymatou.messagebus.facade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自身内容能以可读方式输出
 * @author wangxudong
 *
 */
public abstract class PrintFriendliness implements Serializable {

    private static final long serialVersionUID = -235822080790001901L;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":"
                + JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.SkipTransientField);
    }
}
