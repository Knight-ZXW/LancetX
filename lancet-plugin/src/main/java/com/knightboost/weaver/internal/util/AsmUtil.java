package com.knightboost.weaver.internal.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Created by Knight-ZXW on 17/4/11.
 */
public class AsmUtil {

    public static MethodNode clone(MethodNode node) {
        MethodNode clone = new MethodNode(Opcodes.ASM5, node.access, node.name, node.desc, node.signature,
                (String[]) node.exceptions.toArray(new String[node.exceptions.size()]));
        node.accept(clone);
        return clone;
    }

    /**
     * 获得指定注解的值
     *
     * @param annotationNode
     * @param annotation
     * @return
     */
    public static String findAnnotationStringValue(AnnotationNode annotationNode,
                                            String annotation) {
        List<Object> values = annotationNode.values;
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.size(); i++) {
            if (annotation.equals((String) values.get(i))) {
                return (String) values.get(i + 1);
            }
        }
        return null;
    }
}
