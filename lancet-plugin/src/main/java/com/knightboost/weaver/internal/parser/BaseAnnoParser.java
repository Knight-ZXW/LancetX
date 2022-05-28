package com.knightboost.weaver.internal.parser;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

public abstract class BaseAnnoParser implements AnnoParser {


    /**
     * 获得指定注解的值
     *
     * @param annotationNode
     * @param annotation
     * @return
     */
    public String findAnnotationStringValue(AnnotationNode annotationNode,
                                            String annotation) {
        List<Object> values = annotationNode.values;
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.size(); i++) {
            if (annotation.equals(values.get(i))) {
                return (String) values.get(i + 1);
            }
        }
        return null;
    }

    public Object findAnnotationValue(AnnotationNode annotationNode,
                                                   String annotation){
        List<Object> values = annotationNode.values;
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.size(); i++) {
            if (annotation.equals(values.get(i))) {
                return  values.get(i + 1);
            }
        }
        return null;
    }

    public boolean findAnnotationBoolValue(AnnotationNode annotationNode,
                                           String annotation,boolean defaultValue){
        Object boolValue = findAnnotationValue(annotationNode, annotation);
        if (boolValue == null){
            return defaultValue;
        }
        return (boolean) boolValue;
    }
}
