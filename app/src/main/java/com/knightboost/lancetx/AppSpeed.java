package com.knightboost.lancetx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AppSpeed {
    /**
     * 流程名称
     * @return
     */
    String section() default "";
}
