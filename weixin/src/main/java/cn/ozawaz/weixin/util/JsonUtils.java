package cn.ozawaz.weixin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.util
 * @since JDK1.8
 */
public class JsonUtils {

    @SuppressWarnings("all")
    public static HashMap jsonStringToMap(String body) {
        return JSON.parseObject(body, HashMap.class);
    }

    @SuppressWarnings("all")
    public static String mapToJsonString(Map map) {
        return new JSONObject(map).toString();
    }
}
