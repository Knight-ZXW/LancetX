package com.knightboost.lancet.internal.asm.classvisitor;

import com.knightboost.lancet.internal.entity.ReplaceInfo;
import com.knightboost.lancet.internal.entity.TransformInfo;

import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Knight-ZXW on 17/3/27.
 */
public class ReplaceClassVisitor extends BaseWeaveClassVisitor {

    //do not modify
    private List<ReplaceInfo> replaceInfos;

    private List<ReplaceInfo> matches;


    private TransformInfo transformInfo;

    private String className;

    public ReplaceClassVisitor(TransformInfo transformInfo) {
        this.transformInfo = transformInfo;
        this.replaceInfos = transformInfo.replaceInfo;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        matches = replaceInfos.stream()
                .filter(t -> t.match(name))
                .filter(new Predicate<ReplaceInfo>() {
                    @Override
                    public boolean test(ReplaceInfo replaceInfo) {
                        for (String weaverClass : transformInfo.weaverClasses) {
                            if (name.startsWith(weaverClass)){ //默认内部类不被插桩
                                return false;
                            }
                        }
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (matches == null || matches.size() == 0) {
            return mv;
        }

        return new ReplaceWeaveMethodVisitor(
                mv,
                access, name, desc,
                 matches,
                this.className);
    }

}
