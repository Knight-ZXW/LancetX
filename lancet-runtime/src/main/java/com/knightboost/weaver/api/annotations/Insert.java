package com.knightboost.weaver.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 修改原函数的实现
 *
 * 支持 insert in method body head / insert in method boy end， 并且支持修改值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {
    //default as declared method name
    String value();

    boolean mayCreateSuper() default false;
}
