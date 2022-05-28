package com.knightboost.lancet.lang.annotation;



import com.knightboost.lancet.lang.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
public @interface Target {

    String value();

    //从继承体系角度 控制 Weave 范围
    Scope scope() default Scope.SELF;
}
