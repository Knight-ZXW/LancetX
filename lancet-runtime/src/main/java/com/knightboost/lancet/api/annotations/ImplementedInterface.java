package com.knightboost.lancet.api.annotations;

import com.knightboost.lancet.api.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Knight-ZXW on 17/3/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
public @interface ImplementedInterface {

    String[] value();

    Scope scope() default Scope.SELF;
}
