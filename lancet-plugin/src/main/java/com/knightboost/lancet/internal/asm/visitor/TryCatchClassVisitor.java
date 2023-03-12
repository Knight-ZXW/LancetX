package com.knightboost.lancet.internal.asm.visitor;

import com.knightboost.lancet.internal.entity.TransformInfo;

import org.objectweb.asm.MethodVisitor;

public class TryCatchClassVisitor extends BaseWeaveClassVisitor{

    public TryCatchClassVisitor(TransformInfo transformInfo) {

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
