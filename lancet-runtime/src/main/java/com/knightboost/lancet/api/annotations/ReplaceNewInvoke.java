package com.knightboost.lancet.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ReplaceNewInvoke 用于替换 new XX() 指令。
 *
 * 比如将应用中所有 new Thread() 的调用 替换为 new ProxyThread() ，可以通过以下方式声明
 * ```
 *     @ReplaceNewInvoke()
 *     public static void replaceNewThread(Thread t, ProxyThread proxyThread){
 *
 *     }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReplaceNewInvoke {

}
