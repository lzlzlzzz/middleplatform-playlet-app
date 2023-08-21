package com.hehe.playletapp.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldsFilter {

    /**
     * 字段返回包含集，返回的字段中只包含指定的字段集
     * @return
     */
    String[] includes() default {};

    /**
     * 字段返回过滤集，从返回的所有字段中排除指定字段
     * @return
     */
    String[] excludes() default {};

    /**
     * 是否需要额外的字段前缀, 默认不加情况下即使配置了响应前缀，返回数据也不会加
     * @return
     */
    boolean additionalPrefix() default false;

}
