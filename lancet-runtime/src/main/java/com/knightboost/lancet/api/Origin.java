package com.knightboost.lancet.api;

/**
 * 当代理的类 （target class）是 私有的，无法在 weaver 类中使用 target class，
 * 此时可以用Origin 类代替原有的对象
 */
public class Origin {
    public static final String CLASS_NAME = Origin.class.getName().replace('.', '/');

    private Origin() {
        throw new AssertionError();
    }

    /**
     * 当进行proxy时， Origin表示原来的函数体字节码 ，通过控制 Origin.call() 函数的位置，
     * 我们可以控制在源函数体前插入代码,如：
     *
     * 或者在源函数体后插入代码，如：
     *
     * 甚至完全替换源函数体的
     *
     */
    public static void callVoid() {
    }

    public static <U extends Throwable> void callVoidThrowOne() throws U {
    }

    public static <U extends Throwable, V extends Throwable> void callVoidThrowTwo() throws U, V {
    }

    public static <U extends Throwable, V extends Throwable, W extends Throwable> void callVoidThrowThree() throws U, V, W {
    }

    public static Object call() {
        return new Object();
    }

    public static <V extends Throwable> Object callThrowOne() throws V {
        return new Object();
    }

    public static <V extends Throwable, U extends Throwable> Object callThrowTwo() throws V, U {
        return new Object();
    }

    public static <V extends Throwable, U extends Throwable, W extends Throwable> Object callThrowThree() throws U, V, W {
        return new Object();
    }
}
