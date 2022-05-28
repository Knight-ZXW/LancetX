package com.knightboost.weaver.internal.asm.classvisitor;

import com.knightboost.weaver.api.annotations.WeaveExclude;
import com.knightboost.weaver.internal.log.WeaverLog;
import com.knightboost.weaver.internal.util.TypeUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;

public class HookClassVisitor extends BaseWeaveClassVisitor {


    private static final String WEAVE_EXCLUDE = Type.getDescriptor(WeaveExclude.class);
    private final Set<String> weaveClasses;

    private boolean isWeaveClasses;

    public HookClassVisitor(Set<String> weaveClasses) {
        this.weaveClasses = weaveClasses;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        transformer.methodChain.init(name, this);

        transformer.originClassName = name;
        transformer.superName = superName;

        if (weaveClasses.contains(name)) {
            isWeaveClasses = true;
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
        //如果包含 WeaveIgnore注解则，则直接跳过
        if (descriptor.equals(WEAVE_EXCLUDE)) {
            WeaverLog.tag(this).e(transformer.originClassName + "跳过");
            skipWeaveVisitor();
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isWeaveClasses) {
            return super.visitMethod(TypeUtils.resetAccessScope(access, Opcodes.ACC_PUBLIC), name, desc, signature, exceptions);
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
