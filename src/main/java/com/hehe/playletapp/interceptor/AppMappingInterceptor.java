package com.hehe.playletapp.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hehe.playletapp.response.AppStatus;
import com.hehe.playletapp.util.AESUtil;
import com.hehe.playletapp.entity.PlayletApp;
import com.hehe.playletapp.entity.AppApiInterface;
import com.hehe.playletapp.service.AppApiInterfaceService;
import com.hehe.playletapp.service.IJWTService;
import com.hehe.playletapp.service.PlayletAppService;
import com.hehe.playletapp.util.RedisUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class AppMappingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AppMappingInterceptor.class);

    @Resource
    private IJWTService jwtService;

    @Value("${rsa-aes.app-ae-enckey}")
    private String appAesEnckey;

    @Resource
    private AppApiInterfaceService appApiInterfaceService;

    @Resource
    private PlayletAppService playletAppService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String token = httpServletRequest.getHeader("Authorization");
        logger.info("Authorization Token : {}", token);
        //token 不为空，验证token 数据有效期
        if (StringUtils.isNotEmpty(token)) {
            return tokenValidation(httpServletResponse, token, httpServletRequest);
        } else {
            logger.info("app端请求鉴权失敗，無token, 请求url:{}", httpServletRequest.getRequestURL());
            WriteResponse(httpServletRequest,httpServletResponse, AppStatus.APP_TOKEN_EMPTY);
            return false;
        }
    }

    /**
     * token 不为空校验逻辑方法
     * @param httpServletResponse
     * @param token
     * @return
     */
    private boolean tokenValidation(HttpServletResponse httpServletResponse, String token, HttpServletRequest request) {
        String decResult = "";
        try {
            // 参数匹配逻辑
            String packageName = "";
            String uri = request.getRequestURI();
            Object redisAppId = redisUtil.getKey(uri+"_packageName");
            if(null != redisAppId){
                packageName = redisAppId.toString();
            }else {
                List<AppApiInterface> appApiInterfaceList = appApiInterfaceService.getInfo(uri,"request");
                if(appApiInterfaceList.size()>0){
                    packageName = appApiInterfaceList.get(0).getPackageName();
                    redisUtil.setStr(uri+"_packageName",packageName);
                }
            }
            //获取tokenkey
            Object redisTokenKey = redisUtil.getKey("TokenKey_"+packageName);
            if(null != redisTokenKey){
                decResult = AESUtil.decryptFromBase64(token, redisTokenKey.toString());
            }else {
                PlayletApp byPackageName = playletAppService.getIntegralAppByAppId(packageName);
                if(null != byPackageName){
                    decResult = AESUtil.decryptFromBase64(token, byPackageName.getTokenKey());
                    redisUtil.setStr("TokenKey_"+packageName,byPackageName.getTokenKey());
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.info("token错误, 无法解密.");
            WriteResponse(request,httpServletResponse, AppStatus.APP_TOKEN_INVALID);
            return false;
        }
        if (StringUtils.isNotEmpty(decResult)) {
            Claims claims = null;
            try {
                claims = jwtService.parseJWT(decResult);
                Date expire = claims.getExpiration();
                long expireTime = expire.getTime();
                if (expireTime > System.currentTimeMillis()) {
                    return true;
                } else {
                    logger.info("app端token过期, RequestUrl: {}", request.getRequestURI());
                    httpServletResponse.setHeader("Authorization", token);
                    httpServletResponse.setHeader("Code", String.valueOf(AppStatus.APP_TOKEN_EXPIRED.code()));
                    WriteResponse(request,httpServletResponse, AppStatus.APP_TOKEN_EXPIRED);
                    return false;
                }
            }catch (Exception e) {
                logger.info("app端token过期");
                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Code", "500");
                WriteResponse(request,httpServletResponse, AppStatus.APP_TOKEN_EXPIRED);
                return false;
            }
        }else {
            WriteResponse(request,httpServletResponse, AppStatus.APP_TOKEN_INVALID);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        long severTime = System.currentTimeMillis();
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setHeader("mtr", String.valueOf(severTime));
    }

    /**
     * 从拦截器写返回数据给应用端
     * @param response
     * @param appStatus
     */
    private void WriteResponse(HttpServletRequest request,HttpServletResponse response, AppStatus appStatus) {
        //指名内容类型
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        long severTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        Object redisAppId = redisUtil.getKey(uri+"packageName");
        Object redisResponse = redisUtil.getKey(uri+"_response");
        response.setHeader("mtr", String.valueOf(severTime));
       try {
           Map<String, Object> result = new HashMap();
           String packageName = "";
           if(null != redisAppId && null != redisResponse){
               packageName = redisAppId.toString();
               JSONObject jsonObject = JSONObject.parseObject(redisResponse.toString());
               if(null != jsonObject.getString("code")){
                   result.put(jsonObject.getString("code"), appStatus.code());
               }
               if(null != jsonObject.getString("msg")){
                   result.put(jsonObject.getString("msg"), appStatus.message());
               }
               if(null != jsonObject.getString("data")){
                   result.put(jsonObject.getString("data"), Collections.emptyMap());
               }
           }else {
               // 参数匹配逻辑
               Map responseParam = new HashMap();
               List<AppApiInterface> apiInterfaces = appApiInterfaceService.getInfo(uri,"response");
               if(apiInterfaces.size()>0){
                   packageName = apiInterfaces.get(0).getPackageName();
                   redisUtil.setStr(uri+"_packageName",packageName);
                   for(AppApiInterface appApiInterface:apiInterfaces){
                       String key = appApiInterface.getMappingKey();
                       String value = appApiInterface.getProductMappingAlias();
                       responseParam.put(key, value);
                       if("code".equals(key)){
                           result.put(value, appStatus.code());
                       }
                       if("msg".equals(key)){
                           result.put(value, appStatus.message());
                       }
                       if("data".equals(key)){
                           result.put(value, Collections.emptyMap());
                       }
                   }
                   redisUtil.setStr(uri+"_response",JSONObject.toJSONString(responseParam));
               }
           }
           //获取ResponseKey
//           Object redisResponseKey = redisUtil.getKey("ResponseKey_"+packageName);
//           if(null != redisResponseKey){
//               response.getWriter().write(JSON.toJSONString(result));
//           }else {
//               IntegralApp byPackageName = integralAppService.getIntegralAppByAppId(packageName);
//               if(byPackageName != null){
//                   logger.info("Interceptor Response: {}\n",result);
//                   String responseKey = byPackageName.getResponseKey();
//                   // 入redis
//                   redisUtil.setStr("ResponseKey_"+packageName,responseKey);
//                   response.getWriter().write(JSON.toJSONString(result));
//               }
//           }
           response.getWriter().write(JSON.toJSONString(result));
       }catch (Exception e) {
            e.printStackTrace();
            logger.info("Write Response Data Error.");
       }
    }
}
