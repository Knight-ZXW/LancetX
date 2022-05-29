package com.knightboost.lancet.internal.parser.anno;

import com.knightboost.lancet.internal.meta.TryCatchAnnoMeta;
import com.knightboost.lancet.internal.parser.AnnoParser;
import com.knightboost.lancet.internal.parser.AnnotationMeta;

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
