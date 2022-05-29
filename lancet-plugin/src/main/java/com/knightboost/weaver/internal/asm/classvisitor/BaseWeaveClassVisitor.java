package com.knightboost.weaver.internal.asm.classvisitor;

import com.knightboost.weaver.internal.entity.TransformInfo;
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
