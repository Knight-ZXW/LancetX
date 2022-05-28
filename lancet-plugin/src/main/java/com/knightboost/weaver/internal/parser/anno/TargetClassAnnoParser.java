package com.knightboost.weaver.internal.parser.anno;

import com.google.common.base.Strings;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.internal.exception.IllegalAnnotationException;
import com.knightboost.weaver.internal.meta.TargetClassMeta;
import com.knightboost.weaver.internal.parser.AnnotationMeta;
import com.knightboost.weaver.internal.parser.BaseAnnoParser;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * Created by Knight-ZXW
 */
public class TargetClassAnnoParser extends BaseAnnoParser {

    private static final String ENUM_DESC = Type.getDescriptor(Scope.class);


    @SuppressWarnings("unchecked")
    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {

        String temp = findAnnotationStringValue(annotationNode,"value");
        if (Strings.isNullOrEmpty(temp)) {
            throw new IllegalAnnotationException("@TargetClass value can't be empty or null");
        }

        String[] vs = (String[]) findAnnotationValue(annotationNode,"scope");
        if (!ENUM_DESC.equals(vs[0])) {
            throw new IllegalAnnotationException();
        }

        String className = temp.replace('.', '/');
        Scope scope =  Scope.valueOf(vs[1]);

        return new TargetClassMeta(className,scope,annotationNode.desc);
    }


}
