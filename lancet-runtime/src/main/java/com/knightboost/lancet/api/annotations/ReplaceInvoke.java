package com.knightboost.lancet.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ReplaceInvoke 用于替换函数体中 某个函数的调用,
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReplaceInvoke {
    //默认被赋值为 weave 的函数名
    String targetMethodName() default "";
    //默认值 为 weaver class
    String replaceClassName() default "";
    //默认值 为 weaver method name
    String replaceMethodName() default "";
    //表示目标函数为 静态函数
    boolean isStatic() default false;
}
