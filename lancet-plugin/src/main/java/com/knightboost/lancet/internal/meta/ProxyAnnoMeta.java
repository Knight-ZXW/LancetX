package com.knightboost.lancet.internal.meta;

import com.knightboost.lancet.internal.parser.AnnotationMeta;

public class ProxyAnnoMeta extends AnnotationMeta {

    public final String targetMethod;

    public ProxyAnnoMeta(String targetMethod) {
        this.targetMethod = targetMethod;
    }

}
