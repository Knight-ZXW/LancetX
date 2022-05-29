package com.knightboost.lancetx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO 不依赖AspectJ， 直接在Apm的插件内处理
 * Description:
 *
 * @author zhou.junyou
 * Create by:Android Studio
 * Date:2020/3/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AppSpeed {
    /**
     * 流程名称
     * @return
     */
    String section() default "";
}
