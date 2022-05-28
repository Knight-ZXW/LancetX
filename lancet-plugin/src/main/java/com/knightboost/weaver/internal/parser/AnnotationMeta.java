package com.knightboost.weaver.internal.parser;

/**
 * Created by Knight-ZXW
 */
public abstract class AnnotationMeta {

    public AnnotationMeta() {

    }

    public AnnotationMeta(String targetMethodDesc) {
        this.targetMethodDesc = targetMethodDesc;
    }

    public String targetMethodDesc;
}
