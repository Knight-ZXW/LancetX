package com.knightboost.lancet.internal.graph;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleClassGraph {

    private final Map<String, ClassNode> classNodes = new ConcurrentHashMap<>();

    public void addClass(ClassNode classNode) {
        classNodes.put(classNode.name, classNode);
    }

    public ClassNode get(String className) {
        return classNodes.get(className);
    }

    public boolean exists(String className) {
        return classNodes.containsKey(className);
    }

    public boolean inherit(String childClassName, String parentClassName) {
        if (childClassName == null || parentClassName == null) {
            return false;
        }
        if (childClassName.equals(parentClassName)) {
            return true;
        }

        ClassNode child = classNodes.get(childClassName);
        if (child == null) {
            return false;
        }

        // 检查父类
        if (parentClassName.equals(child.superName)) {
            return true;
        }

        // 递归检查父类
        if (child.superName != null) {
            if (inherit(child.superName, parentClassName)) {
                return true;
            }
        }

        // 检查接口
        if (child.interfaces != null) {
            for (String iface : child.interfaces) {
                if (inherit(iface, parentClassName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<ClassNode> getChildren(String className) {
        List<ClassNode> children = new ArrayList<>();
        for (ClassNode node : classNodes.values()) {
            if (className.equals(node.superName)) {
                children.add(node);
            }
        }
        return children;
    }

    public List<ClassNode> getImplementations(String interfaceName) {
        List<ClassNode> implementations = new ArrayList<>();
        for (ClassNode node : classNodes.values()) {
            if (node.interfaces != null && node.interfaces.contains(interfaceName)) {
                implementations.add(node);
            }
        }
        return implementations;
    }
}
