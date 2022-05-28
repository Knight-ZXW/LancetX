package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.internal.parser.AnnotationMeta;

import java.util.List;

public class ImplementedInterfaceMeta extends AnnotationMeta {

    public final List<String> interfaces;

    public final Scope scope;

    public ImplementedInterfaceMeta(List<String> interfaces, Scope scope) {
        this.interfaces = interfaces;
        this.scope = scope;
    }
}
