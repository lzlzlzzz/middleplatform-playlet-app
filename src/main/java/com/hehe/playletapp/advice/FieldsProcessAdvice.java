package com.hehe.playletapp.advice;

import com.alibaba.fastjson.JSONObject;
import com.hehe.playletapp.annotation.FieldsFilter;
import com.hehe.playletapp.entity.AppApiInterface;
import com.hehe.playletapp.service.AppApiInterfaceService;
import com.hehe.playletapp.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(10)
public class FieldsProcessAdvice implements ResponseBodyAdvice {

    @Resource
    private AppApiInterfaceService appApiInterfaceService;

    @Autowired
    private RedisUtil redisUtil;

    private boolean additionalPrefix = false;


    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        if (Objects.requireNonNull(methodParameter.getMethod()).isAnnotationPresent(FieldsFilter.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (null == o) {
            return o;
        }
        String appId = "";
        Map responseParam = new HashMap();
        Map responseRedundanceParam = new HashMap();
        List<String> responseRedundance = new ArrayList();
        URI uri = serverHttpRequest.getURI();
        String path = uri.getPath();
        Object redisAppId = redisUtil.getKey(path+"_packageName");
        Object redisResponseRedundance = redisUtil.getKey(path+"_responseRedundance");
        Object redisResponse = redisUtil.getKey(path+"_response");
        if(null!=redisAppId){
            appId = redisAppId.toString();
        }
        if(null != redisResponseRedundance){
            JSONObject jsonObject = JSONObject.parseObject(redisResponseRedundance.toString());
            Map<String, Object> responseRedundanceMap = jsonObject.getInnerMap();
            responseRedundanceParam.putAll(responseRedundanceMap);
            responseRedundance = responseRedundanceMap.keySet().stream().collect(Collectors.toList());
        }else {
            // 根据appId，URI，查询返回冗余参数；返回值加冗余
            List<AppApiInterface> appApiInterfaceList = appApiInterfaceService.getInfo(path,"responseRedundance");
            if(appApiInterfaceList.size() > 0) {
                appId = appApiInterfaceList.get(0).getPackageName();
                redisUtil.setStr(path+"_packageName",appId);
                for(AppApiInterface appApiInterface:appApiInterfaceList){
                    String productMappingAlias = appApiInterface.getProductMappingAlias();
                    String mappingKey = appApiInterface.getMappingKey();
                    if (StringUtils.isNotEmpty(productMappingAlias)) {
                        responseRedundance.add(mappingKey);
                        responseRedundanceParam.put(mappingKey, productMappingAlias);
                    }
                }
                redisUtil.setStr(path+"_responseRedundance",JSONObject.toJSONString(responseRedundanceParam));
            }
        }
        if(null != redisResponse){
            JSONObject jsonObject = JSONObject.parseObject(redisResponse.toString());
            Map<String, Object> ResponseMap = jsonObject.getInnerMap();
            responseParam.putAll(ResponseMap);

        }else {
            List<AppApiInterface> apiInterfaces = appApiInterfaceService.getInfo(path,"response");
            if(apiInterfaces.size() > 0) {
                appId = apiInterfaces.get(0).getPackageName();
                for(AppApiInterface param:apiInterfaces){
                    String mappingKey = param.getMappingKey();
                    String productMappingAlias = param.getProductMappingAlias();
                    responseParam.put(mappingKey, productMappingAlias);
                }
                redisUtil.setStr(path+"_response",JSONObject.toJSONString(responseParam));
            }
        }

        if (StringUtils.isEmpty(appId)) {
            return o;
        }
        FieldsFilter methodAnnotation = methodParameter.getMethodAnnotation(FieldsFilter.class);
        if (null != methodAnnotation) {
            additionalPrefix = methodAnnotation.additionalPrefix();

        }
        // 合并返回参数和冗余字段映射
        Map<String, Object> combineResultMap = new HashMap<String, Object>();
        combineResultMap.putAll(responseParam);
        combineResultMap.putAll(responseRedundanceParam);
        if (o instanceof Map) {
            Map map = (Map) o;
            if (map.containsKey("data")) {
                Object data = map.get("data");
                Object status = map.get("code");
                Object message = map.get("msg");
                map.clear();
                if (data instanceof List) {
                    //List
                    List resultList = (List) data;
                    //处理list
                    List list = processList(resultList,responseRedundance,combineResultMap);
                    map.put(responseParam.get("data"), list);
                } else if (data instanceof Map) {
                    //Map
                    Map dataMap = (Map) data;
                    Map resultMap = processMap(dataMap,responseRedundance,combineResultMap);
                    map.put(responseParam.get("data"), resultMap);
                    //冗余字段
                    for(String x : responseRedundance){
                        resultMap.put(responseRedundanceParam.get(x),random());
                    }
                }else if( data instanceof Integer || data instanceof Long || data instanceof Double || data instanceof Float ||
                    data instanceof Boolean || data instanceof String || data instanceof Date || data instanceof BigDecimal){
                    map.put(responseParam.get("data"), data);
                    //冗余字段
                    for(String x : responseRedundance){
                        map.put(responseRedundanceParam.get(x),random());
                    }
                }else {
                    Object o1 = processObject(data,responseRedundance,combineResultMap);
                    if(o1 instanceof Map){
                        Map resultMap = (Map)o1;
                        for(String x : responseRedundance){
                            resultMap.put(responseRedundanceParam.get(x),random());
                        }
                    }
                    map.put(responseParam.get("data"), o1);

                }
                map.put(responseParam.get("code"), status);
                map.put(responseParam.get("msg"), message);
                map.get(responseParam.get("data"));
                return map;
            }
        }
        return o;
    }

    private List processList(List resultList,List<String> responseRedundance,Map responseParam) {
        List list = new ArrayList();
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        for (Object object : resultList) {
            //处理List<Map<String, Object>> 类型，其他的暂不处理
            if (object instanceof Map) {
                Map m = (Map) object;
                Map map = processMap(m, responseRedundance,responseParam);
                list.add(map);
            }else if( object instanceof Integer || object instanceof Long || object instanceof Double || object instanceof Float ||
                    object instanceof Boolean || object instanceof String  || object instanceof Date || object instanceof BigDecimal  ){
                list.add(object);
            } else  {
                Object o = processObject(object,responseRedundance,responseParam);
                list.add(o);
            }
        }
        return list;
    }
    private Map<String, Object> processMap(Map<String, Object> map, List<String> responseRedundance, Map<String, Object> responseParam) {
        Map<String, Object> result = new HashMap();
        if (null == map || map.size() == 0) {
            //冗余字段
            for(String x : responseRedundance){
                result.put(String.valueOf(responseParam.get(x)),random());
            }
            return result;
        }
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            String mappingKey = "";
            if (responseParam.get(key) != null) {
                mappingKey = responseParam.get(key).toString();
            }
            if (StringUtils.isNotEmpty(mappingKey)) {
                result.put(mappingKey, value);
            }
        }
        //冗余字段
        for(String x : responseRedundance){
            result.put(String.valueOf(responseParam.get(x)),random());
        }
        return result;
    }


    private Object processObject(Object o,List<String> responseRedundance,Map<String, Object> responseParam) {
        if (null == o) {
            return o;
        }
        Map<String, Object> result = new HashMap<>();
        Class<?> oClass = o.getClass();
        Field[] declaredFields = oClass.getDeclaredFields();
        int i = declaredFields.length;
        for (Field declaredField : declaredFields) {
            String name = declaredField.getName();
            Object o1 = null;

            try {
                declaredField.setAccessible(true);
                o1 = declaredField.get(o);
                if(o1 instanceof List){
                    //List
                    List resultList = (List) o1;
                    //处理list
                    List list = processList(resultList,responseRedundance,responseParam);
                    for(Map.Entry<String, Object> ma : responseParam.entrySet()){
                        String key0  = ma.getKey();
                        String value0 = ma.getValue().toString();
                        if(key0.equals(name)){
                            result.put(value0, list);
                        }
                    }
                }else if(o1 instanceof Map) {
                    // O1 转成Map
                    //Map
                    Map dataMap = (Map) o1;
                    Map resultMap = processMap(dataMap,responseRedundance,responseParam);
                    for(Map.Entry<String, Object> ma : responseParam.entrySet()){
                        String key0  = ma.getKey();
                        String value0 = ma.getValue().toString();
                        if(key0.equals(name)){
                            result.put(value0, resultMap);
                        }
                    }
                    //冗余字段
                    for(String x : responseRedundance){
                        result.put(String.valueOf(responseParam.get(x)),random());
                    }
                }else if( o1 instanceof Integer || o1 instanceof Long || o1 instanceof Double || o1 instanceof Float ||
                        o1 instanceof Boolean || o1 instanceof String  || o1 instanceof Date || o1 instanceof BigDecimal  ){
                    for(Map.Entry<String, Object> ma : responseParam.entrySet()){
                        String key0  = ma.getKey();
                        String value0 = ma.getValue().toString();
                        if(key0.equals(name)){
                            result.put(value0, o1);
                        }
                    }
                }else {
                    Object newO1 =  processObject(o1,responseRedundance,responseParam);
                    for(Map.Entry<String, Object> ma : responseParam.entrySet()){
                        String key0  = ma.getKey();
                        String value0 = ma.getValue().toString();
                        if(key0.equals(name)){
                            result.put(value0, newO1);
                        }
                    }
                    //冗余字段
                    for(String x : responseRedundance){
                        result.put(String.valueOf(responseParam.get(x)),random());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    // 生成2-5位数的随机数
    private String random(){
        int sum = new Random().nextInt(4);
            sum += 2;
        String BASIC = "0123456789qwertyuiopasdfghjklzxcvbnm";
        char[] basicArray = BASIC.toCharArray();
        Random random = new Random();
        char[] result = new char[sum];
        for (int i = 0; i < result.length; i++) {
            int index = random.nextInt(100) % (basicArray.length);
            result[i] = basicArray[index];
        }
        return new String(result);
    }

}
