package com.knightboost.lancet.api;

/**
 * This类 只能用在 @Insert 模式中；
 * This 对象是 一个钩子，通过该钩子可以获取当前运行时的对象 或者是对象的成员。
 */
public class This {

    public static final String CLASS_NAME = This.class.getName().replace('.', '/');

    public static Object get() {
        return new Object();
    }

    /**
     * 获取对象成员变量
     * @param fieldName
     * @return
     */
    public static Object getField(String fieldName) {
        return new Object();
    }

    /**
     * 修改对象成员变量的值
     * @param field
     * @param fieldName
     */
    public static void putField(Object field, String fieldName) {
    }
}
