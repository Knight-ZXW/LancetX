package com.knightboost.weaver.internal.asm.classvisitor;

import com.knightboost.weaver.internal.entity.ReplaceInfo;
import com.knightboost.weaver.plugin.KnightWeaveContext;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ReplaceWeaveMethodVisitor extends AdviceAdapter {

    private final List<ReplaceInfo> matchMap;
    private final String className;
    private final String methodName;


    private boolean log = false;

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv         the method visitor to which this adapter delegates calls.
     * @param access     the method's access flags (see {@link Opcodes}).
     * @param methodName the method's name.
     * @param desc       the method's descriptor
     */
    public ReplaceWeaveMethodVisitor(
            MethodVisitor mv,
            final int access,
            String methodName,
            final String desc,
            List<ReplaceInfo> matchMap,
            String className) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodDesc =desc;
        this.matchMap = matchMap;
        this.methodName = methodName;
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        //这里频繁生成了集合对象,todo 进行优化，将 owner name desc 建立三级映射 提高匹配的速度

        // owner.name(desc)
        List<ReplaceInfo> replaceInfoList = matchMap.stream()
                .filter(new Predicate<ReplaceInfo>() {
                    @Override
                    public boolean test(ReplaceInfo replaceInfo) {
                        return replaceInfo.targetClassName.equals(owner)
                                && replaceInfo.targetMethodName.equals(name)
                                && replaceInfo.targetMethodDesc.equals(desc);
                    }
                }).collect(Collectors.toList());

        if (replaceInfoList.size() > 0) {
            if (replaceInfoList.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < replaceInfoList.size(); i++) {
                    ReplaceInfo replaceInfo = replaceInfoList.get(i);
                    sb.append(" find in")
                            .append(i)
                            .append(" sourceClass -> ").append(replaceInfo.sourceClass).append(" \n");
                }
                //todo 不允许多于一个的replace注解
                throw new IllegalStateException("replace for " + owner + "." +
                        name + "" + desc + " find multiple "+
                " \n"+" detail info is: \n"+sb.toString());
            }
            ReplaceInfo replaceInfo = replaceInfoList.get(0);

            //方法重载的函数调用不做不修改
            if (opcode == Opcodes.INVOKESPECIAL
                    && name.equals(methodName)
                    && desc.equals(methodDesc)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }

            //是否需要做调整
            //目前只支持 对象函数的替换
            if (opcode == Opcodes.INVOKEVIRTUAL) {
                KnightWeaveContext.instance().getLogger()
                        .i("replace in class: " + this.className
                                + " method body " + methodName + methodDesc
                                + " \n"
                                + "    " + "replace " + owner + "." + name + desc
                                + "    " + " to =>" + replaceInfo.replaceClassName + "." +
                                replaceInfo.replaceMethodName + replaceInfo.replaceMethodDesc

                        );
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        replaceInfo.replaceClassName,
                        replaceInfo.replaceMethodName,
                        replaceInfo.replaceMethodDesc
                );
            } else if (opcode == Opcodes.INVOKESTATIC && replaceInfo.targetIsStatic) {
                //warning
                //todo 目前函数签名的匹配，没有区分是静态函数，还是成员函数.这里存在风险
                KnightWeaveContext.instance().getLogger()
                        .i("replace in class: " + this.className
                                + " method body " + methodName + methodDesc
                                + " \n"
                                + "    " + "replace " + owner + "." + name + desc
                                + "    " + " to =>" + replaceInfo.replaceClassName + "." +
                                replaceInfo.replaceMethodName + replaceInfo.replaceMethodDesc
                        );
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        replaceInfo.replaceClassName,
                        replaceInfo.replaceMethodName,
                        replaceInfo.replaceMethodDesc
                );
                return;
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }

        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }
        //

    }
}
