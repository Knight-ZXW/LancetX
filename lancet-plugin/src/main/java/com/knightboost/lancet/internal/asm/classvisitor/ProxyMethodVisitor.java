package com.knightboost.lancet.internal.asm.classvisitor;

import com.knightboost.lancet.internal.entity.ProxyInfo;
import com.knightboost.lancet.internal.log.WeaverLog;
import com.knightboost.lancet.internal.util.TypeUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

/**
 * Created by Knight-ZXW on 17/4/26.
 */
public class ProxyMethodVisitor extends MethodVisitor {

    private final Map<String, MethodChain.Invoker> invokerMap;
    private final Map<String, List<ProxyInfo>> matchMap;
    private final String className;
    private final String name;
//    private final ClassCollector classCollector;
    private final MethodChain chain;
    private final WeaveTransformer weaveTransformer;

    public ProxyMethodVisitor(MethodChain chain,
                              MethodVisitor mv,
                              Map<String, MethodChain.Invoker> invokerMap,
                              Map<String,
                                      List<ProxyInfo>> matchMap,
                              String className, String name,
                              WeaveTransformer weaveTransformer) {
        super(Opcodes.ASM5, mv);
        this.chain = chain;
        this.invokerMap = invokerMap;
        this.matchMap = matchMap;
        this.className = className;
        this.name = name;
        this.weaveTransformer = weaveTransformer;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        String key = owner + " " + name + " " + desc;
        List<ProxyInfo> infos = matchMap.get(key);
        MethodChain.Invoker invoker = invokerMap.get(key);
        if (invoker != null) {
            invoker.invoke(mv);
        } else if (infos != null && infos.size() > 0) {

            String staticDesc =
                    TypeUtils.descToStatic(opcode == Opcodes.INVOKESTATIC ? Opcodes.ACC_STATIC : 0, desc, owner);
            // begin hook this code.
            chain.headFromProxy(opcode, owner, name, desc);

            String artificialClassname = weaveTransformer.getCanonicalName(WeaveTransformer.AID_INNER_CLASS_NAME);
            ClassVisitor cv = weaveTransformer.getInnerClassVisitor(WeaveTransformer.AID_INNER_CLASS_NAME);

            WeaverLog.tag("transform").i("start weave Call method " + " for " + owner + "." + name + desc +
                    " in " + className + "." + this.name);

            infos.forEach(c -> {
                if (TypeUtils.isStatic(c.sourceMethod.access) != (opcode == Opcodes.INVOKESTATIC)) {
                    throw new IllegalStateException(c.sourceClass + "." + c.sourceMethod.name + " should have the same " +
                            "static flag with " + owner + "." + name);
                }
                WeaverLog.tag("transform").i(
                        " from " + c.sourceClass + "." + c.sourceMethod.name);

                String methodName = c.sourceClass.replace("/", "_")
                        + "_" + c.sourceMethod.name;

                chain.next(artificialClassname,
                        Opcodes.ACC_STATIC, methodName,
                        staticDesc, c.threadLocalNode(), cv);
            });

            invokerMap.put(key, chain.getHead());
            chain.getHead().invoke(mv);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
