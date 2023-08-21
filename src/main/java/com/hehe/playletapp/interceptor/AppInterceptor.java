package com.hehe.playletapp.interceptor;

import com.alibaba.fastjson.JSON;
import com.hehe.playletapp.response.AppStatus;
import com.hehe.playletapp.util.AESUtil;
import com.hehe.playletapp.response.ResponseUtil;
import com.hehe.playletapp.response.PlayletResponse;
import com.hehe.playletapp.service.IJWTService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class AppInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AppInterceptor.class);

    @Resource
    private IJWTService jwtService;

    @Value("${rsa-aes.app-ae-enckey}")
    private String appAesEnckey;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String token = httpServletRequest.getHeader("Authorization");
        logger.info("Authorization Token : {}", token);
        //token 不为空，验证token 数据有效期
        if (StringUtils.isNotEmpty(token)) {
            return tokenValidation(httpServletResponse, token, httpServletRequest);
        } else {
            logger.info("app端请求鉴权失敗，無token, 请求url:{}", httpServletRequest.getRequestURL());
            WriteResponse(httpServletResponse, AppStatus.APP_TOKEN_EMPTY);
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
            decResult = AESUtil.decryptFromBase64(token, appAesEnckey);
        }catch (Exception e) {
            e.printStackTrace();
            logger.info("token错误, 无法解密.");
            WriteResponse(httpServletResponse, AppStatus.APP_TOKEN_INVALID);
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
                    WriteResponse(httpServletResponse, AppStatus.APP_TOKEN_EXPIRED);
                    return false;
                }
            }catch (Exception e) {
                logger.info("app端token过期");
                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Code", "500");
                WriteResponse(httpServletResponse, AppStatus.APP_TOKEN_EXPIRED);
                return false;
            }
        }else {
            WriteResponse(httpServletResponse, AppStatus.APP_TOKEN_INVALID);
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
    private void WriteResponse(HttpServletResponse response, AppStatus appStatus) {
        //指名内容类型
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        long severTime = System.currentTimeMillis();
        response.setHeader("mtr", String.valueOf(severTime));
        try {
            PlayletResponse resultMap = ResponseUtil.WrapUpDataForApp(appStatus,"");
            logger.info("Interceptor Response: {}\n",resultMap);
            response.getWriter().write(JSON.toJSONString(resultMap));
        }catch (Exception e) {
            e.printStackTrace();
            logger.info("Write Response Data Error.");
        }
    }
}
