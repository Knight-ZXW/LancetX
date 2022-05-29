package com.knightboost.lancet.internal.parser.anno;

import com.google.common.base.Strings;
import com.knightboost.lancet.api.annotations.ClassOf;
import com.knightboost.lancet.internal.meta.ClassOfMeta;
import com.knightboost.lancet.internal.parser.AnnoParser;
import com.knightboost.lancet.internal.parser.AnnotationMeta;
import com.knightboost.lancet.internal.exception.IllegalAnnotationException;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Knight-ZXW
 */
public class ClassOfAnnoParser implements AnnoParser {

    private Pattern pattern = Pattern.compile("^(((?![0-9])\\w+\\.)*((?![0-9])\\w+\\$)?(?![0-9])\\w+)((\\[])*)$");

    @SuppressWarnings("unchecked")
    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {
        List<Object> values;
        String className = null;
        int index = 0;
        if ((values = annotationNode.values) != null) {
            for (int i = 0; i < values.size(); i += 2) {
                switch ((String) values.get(i)) {
                    case "value":
                        className = (String) values.get(i + 1);
                        if (Strings.isNullOrEmpty(className)) {
                            throw new IllegalAnnotationException("@ClassOf value can't be empty or null");
                        }

                        break;
                    case ClassOf.INDEX:
                        index = (int) values.get(i + 1);
                        break;
                    default:
                        throw new IllegalAnnotationException();
                }
            }

            Type type = Type.getType(toDesc(className));
            return new ClassOfMeta( index, type);
        }

        throw new IllegalAnnotationException("@ClassOf is illegal, must specify value field");
    }

    private String toDesc(String className) {
        Matcher matcher = pattern.matcher(className);
        if (!matcher.find()) {
            throw new IllegalAnnotationException("value in @ClassOf is not a legal type: " + className);
        }
        String clazz = matcher.group(1);
        String bracket = matcher.group(4);
        StringBuilder sb = new StringBuilder(clazz.length() + 10);
        if (bracket != null) {
            for (int i = 0, j = bracket.length() >> 1; i < j; i++) {
                sb.append('[');
            }
        }
        return sb.append('L').append(clazz.replace('.', '/')).append(';').toString();
    }

}
