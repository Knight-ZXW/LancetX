package com.knightboost.lancet.internal.asm.classvisitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ConstructorMethodAroundVisitor extends MethodVisitor {
    public ConstructorMethodAroundVisitor(int api,MethodVisitor methodVisitor) {
        super(api,methodVisitor);
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (opcode == Opcodes.INVOKESPECIAL){
            return;
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
