package com.livehouse.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Fastjson2 based JSON helper.
 */
public final class JsonUtils {

    private static final JSONWriter.Feature[] WRITER_FEATURES = {
            JSONWriter.Feature.WriteMapNullValue
    };

    private JsonUtils() {
    }

    public static String toJsonString(Object value) {
        return JSON.toJSONString(value, WRITER_FEATURES);
    }

    public static Object parse(String json) {
        return JSON.parse(json);
    }

    public static JSONObject parseObject(String json) {
        return JSON.parseObject(json);
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static <T> T parseObject(String json, Type type) {
        return JSON.parseObject(json, type);
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        return JSON.parseObject(json, typeReference);
    }

    public static JSONArray parseArray(String json) {
        return JSON.parseArray(json);
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    public static JSONObject toJsonObject(Object value) {
        if (value instanceof JSONObject) {
            return ((JSONObject) value).clone();
        }
        return JSON.parseObject(toJsonString(value));
    }
}
