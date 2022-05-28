package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class InsertAnnoMeta extends AnnotationMeta {

    public final String targetMethod;
    public final boolean mayCreateSuper;

    public InsertAnnoMeta(String desc,
                          String targetMethod,
                          boolean mayCreateSuper) {
        super(desc);
        this.targetMethod = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
    }


}