package com.knightboost.lancet.internal.parser.anno;

import com.knightboost.lancet.internal.parser.AcceptableAnnoParser;
import com.knightboost.lancet.internal.parser.AnnotationMeta;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.Arrays;

/**
 * Created by Knight-ZXW
 */
public class GatheredAcceptableAnnoParser implements AcceptableAnnoParser {

    public static GatheredAcceptableAnnoParser newInstance(AcceptableAnnoParser... acceptableAnnoParsers) {
        return new GatheredAcceptableAnnoParser(acceptableAnnoParsers);
    }

    private final AcceptableAnnoParser[] acceptableAnnoParsers;

    private GatheredAcceptableAnnoParser(AcceptableAnnoParser[] acceptableAnnoParsers) {
        this.acceptableAnnoParsers = acceptableAnnoParsers;
    }


    @Override
    public boolean accept(String desc) {
        return Arrays.stream(acceptableAnnoParsers).anyMatch(a -> a.accept(desc));
    }

    @Override
    public AnnotationMeta parseAnnotation(AnnotationNode annotationNode) {
        return Arrays.stream(acceptableAnnoParsers)
                .filter(a -> a.accept(annotationNode.desc))
                .findFirst().get().parseAnnotation(annotationNode);
    }
}
