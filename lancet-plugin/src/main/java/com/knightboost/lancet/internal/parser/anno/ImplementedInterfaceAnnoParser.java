package com.knightboost.lancet.internal.parser.anno;

import com.google.common.base.Strings;
import com.knightboost.lancet.internal.parser.AnnoParser;
import com.knightboost.lancet.internal.util.RefHolder;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.internal.exception.IllegalAnnotationException;
import com.knightboost.lancet.internal.meta.ImplementedInterfaceMeta;
import com.knightboost.lancet.internal.parser.AnnotationMeta;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Knight-ZXW
 */
public class ImplementedInterfaceAnnoParser implements AnnoParser {

    private static final String ENUM_DESC = Type.getDescriptor(Scope.class);

    @SuppressWarnings("unchecked")
    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {
        RefHolder<String[]> interfaces = new RefHolder<>(null);
        RefHolder<Scope> scope = new RefHolder<>(Scope.SELF);

        List<Object> values;
        if ((values = annotationNode.values) != null) {
            for (int i = 0; i < values.size(); i += 2) {
                switch ((String) values.get(i)) {
                    case "value":
                        interfaces.set(contentNonNull(((List<String>) values.get(i + 1)).toArray(new String[0])));
                        break;
                    case "scope":
                        String[] vs = (String[]) values.get(i + 1);
                        if (!ENUM_DESC.equals(vs[0])) {
                            throw new IllegalAnnotationException();
                        }
                        scope.set(Scope.valueOf(vs[1]));
                        break;
                    default:
                        throw new IllegalAnnotationException();
                }
            }

            return new ImplementedInterfaceMeta(Arrays.asList(interfaces.get()) ,scope.get());
        }

        throw new IllegalAnnotationException("@ImplementedInterface is illegal, must specify value field");
    }

    private static String[] contentNonNull(String[] interfaces) {
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (Strings.isNullOrEmpty(interfaces[i])) {
                    throw new IllegalAnnotationException("@ImplementedInterface's value can't be null");
                }
                interfaces[i] = interfaces[i].replace('.', '/');
            }
        }
        return interfaces;
    }

}
