package com.knightboost.weaver.internal.meta;

import com.knightboost.weaver.internal.parser.AnnotationMeta;

public class NameRegexMeta extends AnnotationMeta {

    public String regex;
    public boolean revert;

    public NameRegexMeta(String regex,
                         boolean revert) {
        this.regex = regex;
        this.revert = revert;
    }

}
