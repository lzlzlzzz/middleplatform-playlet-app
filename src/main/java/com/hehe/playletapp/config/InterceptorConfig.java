package com.hehe.playletapp.config;

import com.hehe.playletapp.interceptor.AppInterceptor;
import com.hehe.playletapp.interceptor.AppMappingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

    @Resource
    private AppInterceptor appInterceptor;

    @Resource
    private AppMappingInterceptor appMappingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则，
        // /**表示拦截所有请求
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(appInterceptor)
                //拦截所有
                .addPathPatterns("/**");
//                .excludePathPatterns("/**");
        registry.addInterceptor(appMappingInterceptor)
                //拦截所有
                .addPathPatterns("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/**")
                .excludePathPatterns("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/{clickEvent:clickEvent|onclick|pointEvent|clickWork|clickAction|clickBehavior}/{record:record|writeDown|note|chronicle|registe|tally|reflection|take}")
                .excludePathPatterns("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/{token:token|accessToken|accesstoken|accesskey|secretkey|secretToken}/{get:get|receive|have|collect|obtain|cultivate|derive|poll}")
                .excludePathPatterns("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/{invite:invite|invitation|bringIn|askTo|solicit|seek}/{updateHistory:updateHistory|updateChronicle|upAncient|updateArchaic}")
                .excludePathPatterns("/{offerwall:offerwall|quotationWall|quotedPrice|offerList|offerLetter|priceList}/{v2023:v2023|ver1.0.0|2023.01|onlinev01|offversion1.00|releasev1.0|release2023}/{stsToken:stsToken|ststoken|tokenring|stsring|accesskey|stsaccesskey|stsAccesskey}/{get:get|receive|have|collect|obtain|cultivate|derive|poll}");

    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
