package com.knightboost.weaver.internal.parser.anno;

import com.google.common.base.Strings;
import com.knightboost.weaver.internal.exception.IllegalAnnotationException;
import com.knightboost.weaver.internal.meta.InsertAnnoMeta;
import com.knightboost.weaver.internal.parser.AnnoParser;
import com.knightboost.weaver.internal.parser.AnnotationMeta;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

/**
 * Created by Knight-ZXW on 17/5/5.
 */
public class InsertAnnoParser implements AnnoParser {


    @SuppressWarnings("unchecked")
    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {
        List<Object> values;
        String targetMethod = null;
        boolean mayCreateSuper = false;
        if ((values = annotationNode.values) != null) {
            for (int i = 0; i < values.size(); i += 2) {
                switch ((String) values.get(i)) {
                    case "value":
                        targetMethod = (String) values.get(i + 1);
                        if (Strings.isNullOrEmpty(targetMethod)) {
                            throw new IllegalAnnotationException("@InsertAnnoParser value can't be empty or null");
                        }

                        break;
                    case "mayCreateSuper":
                        mayCreateSuper = (boolean) values.get(i + 1);
                        break;
                    default:
                        throw new IllegalAnnotationException();
                }
            }
            //todo 这里的desc是错误的
            return new InsertAnnoMeta(annotationNode.desc, targetMethod, mayCreateSuper);
        }

        throw new IllegalAnnotationException("@InsertAnnoParser is illegal, must specify value field");
    }

}
