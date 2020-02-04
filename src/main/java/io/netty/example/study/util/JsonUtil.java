package io.netty.example.study.util;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.example.study.common.MessageBody;

/**
 * @author kuangjunlin
 */
public class JsonUtil {
    public static final Gson GSON = new Gson();
    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String jsonStr, Class<T> Clazz) {
        return GSON.fromJson(jsonStr, Clazz);
    }
}
