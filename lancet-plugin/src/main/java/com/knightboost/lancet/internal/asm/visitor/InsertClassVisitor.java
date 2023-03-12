package com.knightboost.lancet.internal.asm.visitor;

import com.knightboost.lancet.internal.entity.InsertInfo;
import com.knightboost.lancet.internal.util.TypeUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertClassVisitor extends BaseWeaveClassVisitor {

    private Map<String, List<InsertInfo>> executeInfos;
    private List<InsertInfo> matched;

    public InsertClassVisitor(Map<String, List<InsertInfo>> executeInfos) {
        this.executeInfos = executeInfos;
    }

    private int version;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        //该类是否有 修改的请求
        this.version = version;
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
                                + transformer.className + "." + name);
                    }
                    methodsMatched.add(e);
                    return true;
                }

                return false;
            });

            //不对native及 抽象函数 进行注入
            if (methodsMatched.size() > 0
                    && (access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {

                String staticDesc = TypeUtils.descToStatic(access, desc, transformer.className);

                ClassVisitor cv = transformer
                        .getInnerClassVisitor(WeaveTransformer.AID_INNER_CLASS_NAME);

                String owner = transformer
                        .getCanonicalName(WeaveTransformer.AID_INNER_CLASS_NAME);
                // 生成原函数的钩子函数
                String newName = "original$"+name;
                boolean isInitMethod = "<init>".equals(name);
                if (isInitMethod){
                    throw new RuntimeException("构造函数暂不支持Insert修改");
                }
                // 暂时不支持 对构造函数进行 insert变更, TODO 待支持

                // 将access 修改为 private
                int newAccess = (access & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC))
                        | Opcodes.ACC_PRIVATE;

                MethodChain chain = transformer.getMethodChain();
                chain.headFromInsert(newAccess, transformer.className, newName, desc);

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
                    chain.next(owner, Opcodes.ACC_STATIC, methodName,
                            staticDesc, insetInfo.cloneMethodNode(), cv);
                });
                chain.fakePreMethod(transformer.className, access, name, desc,
                        signature, exceptions);

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
