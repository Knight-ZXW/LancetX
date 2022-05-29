package com.knightboost.lancet.internal.meta;

import com.knightboost.lancet.internal.parser.AnnotationMeta;

import org.objectweb.asm.tree.MethodNode;

import java.util.List;


public class MethodMetaInfo {

    public MethodNode sourceNode;
    public List<AnnotationMeta> metaList;

    public MethodMetaInfo(MethodNode sourceNode) {
        this.sourceNode = sourceNode;
    }
}
