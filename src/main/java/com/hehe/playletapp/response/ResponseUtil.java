package com.hehe.playletapp.response;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static PlayletResponse success(Object o) {
        PlayletResponse response = new PlayletResponse();
        response.setCode(200);
        response.setMsg("success");
        response.setData(o);
        return response;
    }

    public static PlayletResponse fail(Object o) {
        PlayletResponse response = new PlayletResponse();
        response.setCode(500);
        response.setMsg("failed");
        response.setData(o);
        return response;
    }

    public static PlayletResponse fail(Integer code, String msg, Object o) {
        PlayletResponse response = new PlayletResponse();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(o);
        return response;
    }

    public static PlayletResponse WrapUpDataForApp(AppStatus status, Object object){
        PlayletResponse response = new PlayletResponse();
        response.setCode(status.code());
        response.setMsg(status.message());
        response.setData(object);
        return response;
    }

    public static Map<String,Object> successMap(Object o) {
        Map<String, Object> result = new HashMap();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", o);
        return result;
    }

    public static Map<String,Object> failMap(Object o) {
        Map<String, Object> result = new HashMap();
        result.put("code", 500);
        result.put("msg", "failed");
        result.put("data", o);
        return result;
    }
    public static Map<String,Object> WrapUpDataForAppMap(AppStatus status, Object object){
        Map<String, Object> result = new HashMap();
        result.put("code", status.code());
        result.put("msg", status.message());
        result.put("data", object);
        return result;
    }
}
