package com.knightboost.lancet.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * weave 的方法参数中，如果参数类型是私有的，我们没有权限访问，则可以通过ClassOf 来标识原有的参数的类类型
 *
 *  e.g  hookExecute(@ClassOf "com.internal.A" Object object)
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
public @interface ClassOf {
    String value();

    String INDEX = "index";
}
