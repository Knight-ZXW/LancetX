package com.knightboost.lancet.internal.asm.visitor;

import com.knightboost.lancet.internal.entity.ChangeExtendMeta;
import com.knightboost.lancet.internal.entity.TransformInfo;

import java.util.List;

public class ChangeClassExtendVisitor extends BaseWeaveClassVisitor {

    private final List<ChangeExtendMeta> changeExtendMetas;

    public ChangeClassExtendVisitor(TransformInfo transformInfo) {
        changeExtendMetas = transformInfo.changeExtendMetas;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        for (ChangeExtendMeta extendMeta : changeExtendMetas) {
            if (superName != null && superName.equals(extendMeta.getBeforeExtend())
                    && extendMeta.isMatch(name.replace("/", "."))) {
                superName = extendMeta.getAfterExtend();
            }
        }
        super.visit(version, access,
                name,
                signature,
                superName, interfaces);
    }
}
