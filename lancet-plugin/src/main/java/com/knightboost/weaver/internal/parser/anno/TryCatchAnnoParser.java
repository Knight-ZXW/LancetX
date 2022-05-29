package com.knightboost.weaver.internal.parser.anno;

import com.knightboost.weaver.internal.meta.TryCatchAnnoMeta;
import com.knightboost.weaver.internal.parser.AnnoParser;
import com.knightboost.weaver.internal.parser.AnnotationMeta;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * Created by Knight-ZXW
 */
public class TryCatchAnnoParser implements AnnoParser {

    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {
        return new TryCatchAnnoMeta();
    }

}
