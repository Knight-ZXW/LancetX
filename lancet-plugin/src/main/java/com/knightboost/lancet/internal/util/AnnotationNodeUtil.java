package com.knightboost.lancet.internal.util;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

public class AnnotationNodeUtil {


    /**
     * 获得指定注解的值
     *
     * @param annotationNode
     * @param annotation
     * @return
     */
    public static String getAnnotationStringValue(AnnotationNode annotationNode,
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

    public static int getAnnotationIntValue(AnnotationNode annotationNode,
                                            String annotation, int defaultValue) {
        List<Object> values = annotationNode.values;
        if (values == null) {
            return defaultValue;
        }
        for (int i = 0; i < values.size(); i++) {
            if (annotation.equals(values.get(i))) {
                return (Integer) values.get(i + 1);
            }
        }
        return defaultValue;
    }

    public static Object getAnnotationValue(AnnotationNode annotationNode,
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

    public static boolean getAnnotationBoolValue(AnnotationNode annotationNode,
                                                 String annotation, boolean defaultValue){
        Object boolValue = getAnnotationValue(annotationNode, annotation);
        if (boolValue == null){
            return defaultValue;
        }
        return (boolean) boolValue;
    }
}
