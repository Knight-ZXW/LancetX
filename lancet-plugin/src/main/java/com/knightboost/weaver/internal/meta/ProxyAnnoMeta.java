package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class ProxyAnnoMeta extends AnnotationMeta {

    public final String targetMethod;

    public ProxyAnnoMeta(String targetMethod) {
        this.targetMethod = targetMethod;
    }

}
