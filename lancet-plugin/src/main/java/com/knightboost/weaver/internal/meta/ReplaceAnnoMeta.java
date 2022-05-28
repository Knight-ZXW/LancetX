package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class ReplaceAnnoMeta extends AnnotationMeta {


    public final String targetMethodName;

    public final String replaceClassName;
    public final String replaceMethodName;
    public final String replaceMethodDesc;
    public  boolean isStatic;


    public ReplaceAnnoMeta(
            String targetMethodName,
            String targetMethodDesc,
            String replaceClassName,
            String replaceMethodName,
            String replaceMethodDesc,
            boolean isStatic
    ) {
        super(targetMethodDesc);
        this.targetMethodName = targetMethodName;
        this.replaceClassName = replaceClassName;
        this.replaceMethodName = replaceMethodName;
        this.replaceMethodDesc = replaceMethodDesc;
        this.isStatic = isStatic;
    }

}
