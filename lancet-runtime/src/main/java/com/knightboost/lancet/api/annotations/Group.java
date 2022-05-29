package com.knightboost.lancet.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * weaver group
 *
 * 通过分组，来实现 针对性开启某些 字节码插桩功能的效果
 *
 *  在gradle 中 配置 group对应的开关，可以实现该组内字节码插桩功能是否开启
 *
 * ```
 * weave{
 *     incremental false
 *
 *     weaveGroup {
 *         aGroupName {
 *             enable = true
 *         }
 *     }
 *
 * }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
public @interface Group {
    String value();
}
