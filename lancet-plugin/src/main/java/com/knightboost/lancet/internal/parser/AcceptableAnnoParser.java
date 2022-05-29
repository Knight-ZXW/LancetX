package com.knightboost.lancet.internal.parser;

/**
 * Created by Knight-ZXW on 17/5/3.
 */
public interface AcceptableAnnoParser extends AnnoParser {

    boolean accept(String desc);
}
