package com.knightboost.weaver.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 该注解用于标识 限制 哪些类进行或者不进行字节码操作
 * 可以和 replaceInvoke 配合 控制asm操作范围
 *
 * Created by Knight-ZXW on 17/3/21.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface NameRegex {
    String value();
    boolean reverse() default false;
}
