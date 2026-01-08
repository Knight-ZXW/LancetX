package com.knightboost.lancet.internal.asm.visitor;

import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Field;

public class LancetClassVisitorChain {

    private ClassVisitor tail;

    public void connect(ClassVisitor visitor) {
        if (tail == null) {
            tail = visitor;
        } else {
            // 使用反射设置 cv 字段，因为它是 protected 的
            try {
                Field cvField = ClassVisitor.class.getDeclaredField("cv");
                cvField.setAccessible(true);
                cvField.set(visitor, tail);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set cv field", e);
            }
            tail = visitor;
        }
    }

    public ClassVisitor getHead() {
        return tail;
    }
}
