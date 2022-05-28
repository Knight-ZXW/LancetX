package com.knightboost.weaver.internal.asm.classvisitor;

import com.knightboost.weaver.internal.entity.ProxyInfo;

import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProxyClassVisitor extends BaseWeaveClassVisitor {



    private List<ProxyInfo> infos;

    private Map<String, List<ProxyInfo>> matches;

    private Map<String, MethodChain.Invoker> maps = new HashMap<>();


    public ProxyClassVisitor(List<ProxyInfo> infos) {
        this.infos = infos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        matches = infos.stream()
                .filter(t -> t.match(name))
                .collect(Collectors.groupingBy(t -> t.targetClass + " " + t.targetMethod + " " + t.targetDesc));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        // 发现匹配的函数时
        // 1.生成原函数的代理函数
        // 2.修改原函数内容
        // 3.生成内部类
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (matches.size() > 0) {
            mv = new NewProxyMethodVisitor(transformer.methodChain,
                    mv,
                    maps,
                    matches,
                    transformer.originClassName,
                    name,
                    transformer);
        }
        return mv;
    }
}
