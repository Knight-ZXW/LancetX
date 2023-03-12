package com.knightboost.lancet.internal.asm.visitor;

import com.knightboost.lancet.internal.entity.ReplaceInvokeInfo;
import com.knightboost.lancet.internal.entity.TransformInfo;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class ReplaceNewClassVisitor extends BaseWeaveClassVisitor{
    private final List<ReplaceInvokeInfo> replaceInvokes;

    public ReplaceNewClassVisitor(TransformInfo transformInfo){
        replaceInvokes = transformInfo.replaceInvokes;

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new ReplaceNewInstructionVisitor(methodVisitor);
    }


    class ReplaceNewInstructionVisitor  extends MethodVisitor{

        private ReplaceInvokeInfo replaceInvokeInfo = null;
        public ReplaceNewInstructionVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM5, methodVisitor);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {

            if (opcode == Opcodes.NEW){
                for (ReplaceInvokeInfo invokeInfo : replaceInvokes) {
                    if (invokeInfo.getTargetClassType().equals(type)){
                        String newType = invokeInfo.getNewClassType();
                        replaceInvokeInfo = invokeInfo;
                        type = newType;
                        break;
                    }
                }
            }
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (replaceInvokeInfo!=null && "<init>".equals(name)  && owner.equals(replaceInvokeInfo.getTargetClassType())){
                owner = replaceInvokeInfo.getNewClassType();
            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
