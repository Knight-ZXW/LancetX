package com.knightboost.weaver.internal.asm.classvisitor;

import com.knightboost.weaver.internal.entity.InsertInfo;
import com.knightboost.weaver.internal.util.TypeUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO rename to `ASMMethodProxy`?
 * Created by Knight-ZXW on 17/3/27.
 */
public class InsertClassVisitor extends BaseWeaveClassVisitor {

    private Map<String, List<InsertInfo>> executeInfos;
    private List<InsertInfo> matched;

    public InsertClassVisitor(Map<String, List<InsertInfo>> executeInfos) {
        this.executeInfos = executeInfos;

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        //该类是否有 修改的请求
        matched = executeInfos.get(name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (matched != null) {
            List<InsertInfo> methodsMatched = new ArrayList<>(matched.size() >> 1);
            matched.removeIf(e -> {
                if (e.targetMethod.equals(name) && e.targetDesc.equals(desc)) {
                    if (((e.sourceMethod.access ^ access) & Opcodes.ACC_STATIC) != 0) {
                        throw new IllegalStateException(e.sourceClass + "." + e.sourceMethod.name + " must have same static flag as "
                                + transformer.originClassName + "." + name);
                    }
                    methodsMatched.add(e);
                    return true;
                }

                return false;
            });

            // 如果当前函数是 native函数 直接返回，暂不支持对native 函数进行字节码代理

            //不对native及抽象函数 进行注入
            if (methodsMatched.size() > 0
                    && (access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {

                String staticDesc = TypeUtils.descToStatic(access, desc, transformer.originClassName);
                ClassVisitor cv = transformer
                        .getInnerClassVisitor(WeaveTransformer.AID_INNER_CLASS_NAME);

                String owner = transformer
                        .getCanonicalName(WeaveTransformer.AID_INNER_CLASS_NAME);
                // 生成原函数的钩子函数
                String newName = name + "$_original_";

                // 将access 修改为 private
                int newAccess = (access & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC))
                        | Opcodes.ACC_PRIVATE;

                MethodChain chain = transformer.methodChain;
                chain.headFromInsert(newAccess, transformer.originClassName, newName, desc);

                /**
                 * TODO 对于一个函数 可以代理修改多次
                 */
                methodsMatched.forEach(insetInfo -> {
                    String methodName = insetInfo
                            .sourceClass
                            .substring(insetInfo.sourceClass.lastIndexOf("/") + 1)
                            + "_"
                            + insetInfo.sourceMethod.name;
                    //生成 新函数
                    chain.next(owner, Opcodes.ACC_STATIC, methodName, staticDesc, insetInfo.cloneMethodNode(), cv);
                });
                chain.fakePreMethod(transformer.originClassName, access, name, desc, signature, exceptions);

                //将原来的函数进行重名
                return super.visitMethod(newAccess, newName, desc, signature, exceptions);
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        //添加插入的函数
        if (matched != null && matched.size() > 0) {
            new ArrayList<>(matched).stream()
                    .collect(Collectors.groupingBy(e -> e.targetMethod)).forEach((k, v) -> {
                if (v.stream().anyMatch(e -> e.createSuper)) {

                    InsertInfo insertInfo = v.get(0);
                    MethodVisitor mv = visitMethod(insertInfo.sourceMethod.access,
                            insertInfo.targetMethod,
                            insertInfo.targetDesc,
                            insertInfo.sourceMethod.signature,
                            insertInfo.sourceMethod.exceptions.toArray(new String[0]));
                    GeneratorAdapter adapter = new GeneratorAdapter(mv, insertInfo.sourceMethod.access,
                            insertInfo.targetMethod, insertInfo.targetDesc);
                    adapter.visitCode();
                    adapter.loadThis();
                    adapter.loadArgs();
                    adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, transformer.superName,
                            insertInfo.targetMethod, insertInfo.targetDesc, false);
                    adapter.returnValue();
                    int sz = Type.getArgumentsAndReturnSizes(insertInfo.targetDesc);
                    adapter.visitMaxs(Math.max(sz >> 2, sz & 3), sz >> 2);
                    adapter.visitEnd();
                }
            });

        }
        super.visitEnd();
    }
}
