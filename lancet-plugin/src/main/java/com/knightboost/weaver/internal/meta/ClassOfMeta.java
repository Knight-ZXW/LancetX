package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

import org.objectweb.asm.Type;

public class ClassOfMeta extends AnnotationMeta {

    public final int index;
    public final Type type;

    public ClassOfMeta( int index, Type type) {
        this.index = index;
        this.type = type;
    }

    public String internalClassName(Type type) {
        if (type.getSort() == Type.OBJECT) {
            return type.getInternalName();
        } else { // array
            return type.getElementType().getInternalName();
        }
    }
}
