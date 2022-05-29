package com.knightboost.lancet.internal.parser;

import org.objectweb.asm.tree.AnnotationNode;


/**
 * parse and return annotation meta information
 * Created by Knight-ZXW on 17/5/3.
 */
public interface AnnoParser {
    AnnotationMeta parseAnnotation(AnnotationNode annotationNode);
}
