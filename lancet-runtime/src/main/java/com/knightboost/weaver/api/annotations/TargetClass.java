package com.knightboost.weaver.api.annotations;

import com.knightboost.weaver.api.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Knight-ZXW on 17/3/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
public @interface TargetClass {
    String value();

    //从继承体系角度 控制  weave 操作范围
    Scope scope() default Scope.SELF;
}
