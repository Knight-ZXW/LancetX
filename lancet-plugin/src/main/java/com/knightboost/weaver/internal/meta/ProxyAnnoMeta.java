package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class ProxyAnnoMeta extends AnnotationMeta {

    public final String targetMethod;

    public ProxyAnnoMeta(String desc, String targetMethod) {
        super(desc);
        this.targetMethod = targetMethod;
    }

}
