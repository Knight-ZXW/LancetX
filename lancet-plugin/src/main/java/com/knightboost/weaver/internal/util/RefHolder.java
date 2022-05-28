package com.knightboost.weaver.internal.util;

/**
 * Created by Knight-ZXW on 17/5/5.
 */
public class RefHolder<T> {

    private T val;

    public RefHolder(T val) {
        this.val = val;
    }

    public void set(T val) {
        this.val = val;
    }

    public T get() {
        return val;
    }
}
