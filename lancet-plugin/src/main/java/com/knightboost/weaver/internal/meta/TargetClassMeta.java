package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class TargetClassMeta extends AnnotationMeta {

    public String className;

    public Scope scope;

    public TargetClassMeta(String className,
                           Scope scope) {
        this.className = className;
        this.scope = scope;
    }

}
