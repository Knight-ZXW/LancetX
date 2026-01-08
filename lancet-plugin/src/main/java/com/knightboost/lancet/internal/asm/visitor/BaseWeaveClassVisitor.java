package com.knightboost.lancet.internal.asm.visitor;

import org.objectweb.asm.ClassVisitor;

public class BaseWeaveClassVisitor extends ClassVisitor {

    public ClassVisitor nextClassVisitor;

    public WeaveTransformer transformer;

    public BaseWeaveClassVisitor() {
        super(org.objectweb.asm.Opcodes.ASM9);
    }

    public BaseWeaveClassVisitor(ClassVisitor cv) {
        super(org.objectweb.asm.Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
    }

    public void setNext(ClassVisitor cv) {
        this.cv = cv;
        nextClassVisitor = cv;
    }
}
