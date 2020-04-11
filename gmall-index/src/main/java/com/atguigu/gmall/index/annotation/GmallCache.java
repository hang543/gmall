package com.atguigu.gmall.index.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/10
 * @Content:  配置redis缓存注解
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//@Inherited // 子类可继承
@Documented
public @interface GmallCache {

    @AliasFor("prefix")
    String value() default "";

    /**
     * 缓存前缀
     * @return
     */
    @AliasFor("value")
    String prefix() default "";

    /**
     * 以分为单位
      * @return
     */
    int timeout() default 5;

    /**
     * 防止雪崩指定的随机值范围
     * @return
     */
    int random() default 5;

}
