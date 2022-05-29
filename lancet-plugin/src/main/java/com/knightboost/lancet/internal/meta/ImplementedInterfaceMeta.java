package com.knightboost.lancet.internal.meta;

import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.internal.parser.AnnotationMeta;

import java.util.List;

public class ImplementedInterfaceMeta extends AnnotationMeta {

    public final List<String> interfaces;

    public final Scope scope;

    public ImplementedInterfaceMeta(List<String> interfaces, Scope scope) {
        this.interfaces = interfaces;
        this.scope = scope;
    }
}
