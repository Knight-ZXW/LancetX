package com.knightboost.weaver.api.annotations;

/**
 * 该注解用于标识 限制 哪些类不进行字节码修改操作
 */
public @interface WeaveOfClasses {
    // 从字节码所在的类名、包名 控制weave 操作范围
    String classNameRegex() default "";

    //反转匹配结果
    boolean reverse() default false;
}
