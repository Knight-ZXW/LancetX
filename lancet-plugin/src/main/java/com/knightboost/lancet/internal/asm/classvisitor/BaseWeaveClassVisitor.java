package com.knightboost.lancet.internal.asm.classvisitor;

import com.ss.android.ugc.bytex.common.visitor.BaseClassVisitor;

import org.objectweb.asm.ClassVisitor;

public class BaseWeaveClassVisitor extends BaseClassVisitor {

    public ClassVisitor nextClassVisitor;

    public WeaveTransformer transformer;



    @Override
    public void setNext(ClassVisitor cv) {
        super.setNext(cv);
        nextClassVisitor = cv;
    }



}
