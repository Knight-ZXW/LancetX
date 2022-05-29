package com.knightboost.lancet.internal.asm.classvisitor;

import com.knightboost.lancet.internal.util.TypeUtils;
import com.knightboost.lancet.internal.entity.TransformInfo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HookClassVisitor extends BaseWeaveClassVisitor {


    private final TransformInfo transformInfo;
    private boolean isWeaveClass;
    private ClassVisitor originalClassVisitor;

    public HookClassVisitor(TransformInfo transformInfo, OriginalClassVisitor originalClassVisitor) {
        this.transformInfo =  transformInfo;
        this.originalClassVisitor = originalClassVisitor;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        transformer.getMethodChain().init(name, originalClassVisitor);
        transformer.className = name;
        transformer.superName = superName;
        if (transformInfo.isWeaverClass(name)){
            isWeaveClass = true;
            skipWeaveVisitor();
            //这个WeaveClass ，不对weaveClass做任何字节码插桩操作
            //并且修改 类，只能直接继承 Object
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    private boolean skipped = false;

    private void skipWeaveVisitor() {
        if (skipped)
            return;
        this.cv = transformer
                .tailVisitor
                .nextClassVisitor;

        skipped = true;
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isWeaveClass) {
            return super.visitMethod(TypeUtils.resetAccessScope(access, Opcodes.ACC_PUBLIC),
                    name, desc, signature, exceptions);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        transformer.generateInnerClasses();
    }
}
