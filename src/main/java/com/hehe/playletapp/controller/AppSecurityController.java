package com.hehe.playletapp.controller;

import com.alibaba.fastjson.JSONObject;
import com.hehe.playletapp.response.AppStatus;
import com.hehe.playletapp.util.AESUtil;
import com.hehe.playletapp.util.JwtUtil;
import com.hehe.playletapp.util.MD5Util;
import com.hehe.playletapp.util.RandomUtil;
import com.hehe.playletapp.annotation.FieldsFilter;
import com.hehe.playletapp.entity.AppApiInterface;
import com.hehe.playletapp.entity.PlayletApp;
import com.hehe.playletapp.response.ResponseUtil;
import com.hehe.playletapp.response.PlayletResponse;
import com.hehe.playletapp.service.AppApiInterfaceService;
import com.hehe.playletapp.service.PlayletAppService;
import com.hehe.playletapp.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Api(description = "app端-取token相关接口")
@Slf4j
public class AppSecurityController {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(AppSecurityController.class);

    /**
     * jwt token 过期时间 1 小时，默认和用户断开时间一致
     */
    public static final long APP_TOKEN_EXPIRE_PERIOD = 1000 * 60 * 60;

    public static final long APP_TOKEN_EXPIRE_ALLOW_PERIOD = 1000 * 60 * 2;

    private static final String Md5Salt = "zeiu";


    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.baseKey}")
    private String baseKey;

    @Value("${rsa-aes.app-ae-deckey}")
    private String appAesDeckey;

    @Value("${rsa-aes.app-ae-enckey}")
    private String appAesEnckey;

    @Resource
    private AppApiInterfaceService appApiInterfaceService;

    @Resource
    private PlayletAppService playletAppService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * app 取token接口
     * 验证流程
     * 1、检查数据有无修改，重新mD5 sg 和sgv比较，相同则无篡改
     * 2、验证app端请求到服务器时间是否超过2分钟， 超过则不返回token
     * 3、验证包名是否是自有应用的包名
     * @return
     */
    @PostMapping("/*integral*/v*.*.*/getToken")
    @ApiOperation(value = "app取token接口", notes = "app取token接口", response = Map.class)
    public synchronized PlayletResponse getAccessToken(HttpServletResponse response,
                                                        HttpServletRequest request,
                                                        @RequestBody String appEncData) {
        String trackingUid = RandomUtil.uuidString();
        logger.info("{} \"{}\" Tid : {} ", request.getMethod(), request.getRequestURI(), trackingUid);
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotEmpty(appEncData)) {
            String decryptData = "";
            //取app 端的传参
            try {
                decryptData = AESUtil.decryptFromBase64(appEncData, appAesDeckey);
            }catch (Exception e) {
                log.error(e.getMessage(),e);
                logger.info("取token数据解密失败！原始数据: {}", appEncData);
                return ResponseUtil.WrapUpDataForApp(AppStatus.APP_GET_TOKEN_DECRYPTED_FAILURE, null);
            }
            JSONObject parObj = JSONObject.parseObject(decryptData);
            if (null != parObj) {
                //app 包的签名
                String sg = parObj.getString("sg");
                //app 包名
                String appId = parObj.getString("appId");
                //app 请求token 时的时间戳， 服务端
                String timeStamp = parObj.getString("timeStamp");
                //重新MD5 Sg 之后的值
                String sgv = parObj.getString("sgv");
                //任意参数为空，返回null
                if (StringUtils.isEmpty(sg) || StringUtils.isEmpty(appId) ||
                        StringUtils.isEmpty(timeStamp) || StringUtils.isEmpty(sgv)) {
                    return null;
                }
                List<String> list = new ArrayList<>();
                list.add(sg);
                list.add(appId);
                list.add(timeStamp);
                list = list.stream().sorted().collect(Collectors.toList());
                logger.info("sorted list :{}", list);
                String joinStr = StringUtils.join(list, Md5Salt, 0, list.size());
                // 第一次md5
                String md51Result = MD5Util.md5(joinStr);
                logger.info("first origin string  : {}, first MD5 result:{}", joinStr, md51Result);
                if (md51Result.equals(sgv)) {
                    long current = System.currentTimeMillis();
//                    logger.info("当前服务器时间 : {}", current);
                    //数据验证成功，开始下一步验证
                    long time  = Long.valueOf(timeStamp).longValue();
//                    long expTime = time + 60 * 2 * 1000;
                    //时间戳验证成功
                    if (Math.abs(current - time) < APP_TOKEN_EXPIRE_ALLOW_PERIOD) {
                        //验证下一步 apk name
                        PlayletApp byAppId = playletAppService.getIntegralAppByAppId(appId);
                        if (null != byAppId) {
                            String serverPackageName = byAppId.getPackageName();
                            if (StringUtils.isNotEmpty(serverPackageName) && serverPackageName.equals(appId)) {
                                //验证成功，返回accesstoken
                                try {
                                    String uid = UUID.randomUUID().toString();
                                    String jwtToken = JwtUtil.createJWT(uid, "IntegralApp", APP_TOKEN_EXPIRE_PERIOD, baseKey, issuer);
                                    String encryptToken = AESUtil.encryptToBase64(jwtToken, appAesEnckey);
                                    result.put("token", encryptToken);
                                    result.put("expireTime", System.currentTimeMillis() + APP_TOKEN_EXPIRE_PERIOD);
                                    response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                                    return ResponseUtil.WrapUpDataForApp(AppStatus.SUCCESS, result);
                                } catch (Exception e) {
                                    log.error(e.getMessage(),e);
                                }
                            } else {
                                response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                                return ResponseUtil.WrapUpDataForApp(AppStatus.APP_GET_TOKEN_PACKAGENAME_INVALID, null);
                            }
                        }
                    } else {
                        logger.info("app端提交时间戳和服务器时间对比超时");
                        response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                        return ResponseUtil.WrapUpDataForApp(AppStatus.APP_GET_TOKEN_TIME_INVALID, null);
                    }
                } else {
                    logger.info("app提交md5 sgv签名有误,服务器无法验证！");
                    response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                    //验证数据不正确
                    return ResponseUtil.WrapUpDataForApp(AppStatus.APP_GET_TOKEN_SIGN_INVALID, null);
                }
            }
        }
        response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
        return ResponseUtil.WrapUpDataForApp(AppStatus.APP_GET_TOKEN_DATA_EMPTY, null);
    }


    @PostMapping("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/{token:token|accessToken|accesstoken|accesskey|secretkey|secretToken}/{get:get|receive|have|collect|obtain|cultivate|derive|poll}")
    @ApiOperation(value = "app取token接口", notes = "app取token接口", response = Map.class)
    @FieldsFilter(additionalPrefix = true)
    public synchronized Map<String,Object> getToken(HttpServletResponse response,
                                                        HttpServletRequest request,
                                                        @RequestBody String appEncData) {
        String trackingUid = RandomUtil.uuidString();
        logger.info("{} \"{}\" Tid : {} ", request.getMethod(), request.getRequestURI(), trackingUid);
        Map<String, Object> result = new HashMap<>();
        // 参数匹配逻辑
        String packageName = "";
        String uri = request.getRequestURI();
        Object redisAppId = redisUtil.getKey(uri+"_packageName");
        if(null != redisAppId){
            packageName = redisAppId.toString();
        }else {
            List<AppApiInterface> appApiInterfaceList = appApiInterfaceService.getInfo(uri,"path");
            if(appApiInterfaceList.size()>0){
                packageName = appApiInterfaceList.get(0).getPackageName();
                // appId 查到了更新redis
                redisUtil.setStr(uri+"_packageName", packageName);
            }
        }
        PlayletApp byPackageName = playletAppService.getIntegralAppByAppId(packageName);
        if (null != byPackageName) {
            if (StringUtils.isNotEmpty(appEncData)) {
                String decryptData = "";
                //取app 端的传参
                try {
                    if(StringUtils.isNotEmpty(byPackageName.getResponseKey())){
                        decryptData = AESUtil.decryptFromBase64(appEncData, byPackageName.getResponseKey());
                    }
                }catch (Exception e) {
                    log.error(e.getMessage(),e);
                    logger.info("取token数据解密失败！原始数据: {}", appEncData);
                    return ResponseUtil.WrapUpDataForAppMap(AppStatus.APP_GET_TOKEN_DECRYPTED_FAILURE, null);
                }
                JSONObject parObj = JSONObject.parseObject(decryptData);
                if (null != parObj) {
                    //app 包的签名
                    String sg = parObj.getString("sg");
                    //app 包名
                    String appId = parObj.getString("appId");
                    //app 请求token 时的时间戳， 服务端
                    String timeStamp = parObj.getString("timeStamp");
                    //重新MD5 Sg 之后的值
                    String sgv = parObj.getString("sgv");
                    //任意参数为空，返回null
                    if (StringUtils.isEmpty(sg) || StringUtils.isEmpty(appId) ||
                            StringUtils.isEmpty(timeStamp) || StringUtils.isEmpty(sgv)) {
                        return null;
                    }
                    List<String> list = new ArrayList<>();
                    list.add(sg);
                    list.add(appId);
                    list.add(timeStamp);
                    list = list.stream().sorted().collect(Collectors.toList());
                    logger.info("sorted list :{}", list);
                    String joinStr = StringUtils.join(list, Md5Salt, 0, list.size());
                    // 第一次md5
                    String md51Result = MD5Util.md5(joinStr);
                    logger.info("first origin string  : {}, first MD5 result:{}", joinStr, md51Result);
                    if (md51Result.equals(sgv)) {
                        long current = System.currentTimeMillis();
//                    logger.info("当前服务器时间 : {}", current);
                        //数据验证成功，开始下一步验证
                        long time  = Long.valueOf(timeStamp).longValue();
//                    long expTime = time + 60 * 2 * 1000;
                        //时间戳验证成功
                        if (Math.abs(current - time) < APP_TOKEN_EXPIRE_ALLOW_PERIOD) {
                            //验证下一步 apk name
//                            IntegralApp byAppId = integralAppService.getIntegralAppByAppId(appId);
//                            if (null != byAppId) {
                                String serverPackageName = byPackageName.getPackageName();
                                if (StringUtils.isNotEmpty(serverPackageName) && serverPackageName.equals(appId)) {
                                    //验证成功，返回accesstoken
                                    try {
                                        String uid = UUID.randomUUID().toString();
                                        String jwtToken = JwtUtil.createJWT(uid, "IntegralApp", APP_TOKEN_EXPIRE_PERIOD, baseKey, issuer);
                                        String encryptToken = AESUtil.encryptToBase64(jwtToken, byPackageName.getTokenKey());
                                        result.put("token", encryptToken);
                                        result.put("expireTime", System.currentTimeMillis() + APP_TOKEN_EXPIRE_PERIOD);
                                        response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                                        return ResponseUtil.WrapUpDataForAppMap(AppStatus.SUCCESS, result);
                                    } catch (Exception e) {
                                        log.error(e.getMessage(),e);
                                    }
                                } else {
                                    response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                                    return ResponseUtil.WrapUpDataForAppMap(AppStatus.APP_GET_TOKEN_PACKAGENAME_INVALID, null);
                                }
//                            }
                        } else {
                            logger.info("app端提交时间戳和服务器时间对比超时");
                            response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                            return ResponseUtil.WrapUpDataForAppMap(AppStatus.APP_GET_TOKEN_TIME_INVALID, null);
                        }
                    } else {
                        logger.info("app提交md5 sgv签名有误,服务器无法验证！");
                        response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
                        //验证数据不正确
                        return ResponseUtil.WrapUpDataForAppMap(AppStatus.APP_GET_TOKEN_SIGN_INVALID, null);
                    }
                }
            }
        }
        response.setHeader("mrt", String.valueOf(System.currentTimeMillis()));
        return ResponseUtil.WrapUpDataForAppMap(AppStatus.APP_GET_TOKEN_DATA_EMPTY, null);
    }


    public static void main(String[] args) throws Exception {
        String jwtToken = JwtUtil.createJWT( UUID.randomUUID().toString(), "IntegralApp", APP_TOKEN_EXPIRE_PERIOD*24, "hehemiddle", "middleTech");
        String encryptToken = AESUtil.encryptToBase64(jwtToken, "SNLSCVWOTJJYOIFS");
        System.out.println(encryptToken);

//        List<String> list = new ArrayList<>();
//        long current = System.currentTimeMillis();
//        System.out.println(current);
//        list.add("88888888");
//        list.add("com.earn.moneytorun");
//        list.add(Long.toString(current));
//        list = list.stream().sorted().collect(Collectors.toList());
//        String joinStr = StringUtils.join(list, Md5Salt, 0, list.size());
//        // 第一次md5
//        String md51Result = MD5Util.md5(joinStr);//4e35b86201322dc77d1710cfd97c2bc0
//        String svg = md51Result;
//        Map<String,String> map = new HashMap<>();
//        map.put("sg","88888888");
//        map.put("appId","com.earn.moneytorun");
//        map.put("timeStamp",Long.toString(current));
//        map.put("sgv",md51Result);
//        String key = AESUtil.encryptToBase64(JSONObject.toJSONString(map),"0ANU8D5N0G5M3AYX");
//        System.out.println(key);
    }
}
