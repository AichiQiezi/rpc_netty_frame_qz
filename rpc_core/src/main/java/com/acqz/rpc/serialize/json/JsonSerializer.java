package com.acqz.rpc.serialize.json;

import cn.hutool.json.JSONUtil;
import com.acqz.rpc.serialize.Serializer;

import java.nio.charset.StandardCharsets;

/**
 * @author haofeng
 * @date 2023/3/7 16:49
 * @description json
 */

public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        String s = JSONUtil.toJsonStr(obj);
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        T t = JSONUtil.toBean(new String(bytes), clazz);
        return t;
    }
}
