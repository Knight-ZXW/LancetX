package com.knightboost.lancet.internal.graph;

import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.internal.log.WeaverLog;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.function.Consumer;

public class GraphUtil {

    public static NodeVisitor childrenOf(SimpleClassGraph graph,
                                         String className,
                                         Scope scope) {
        return visitor -> {
            ClassNode node = graph.get(className);
            if (node == null) {
                WeaverLog.e("Weaver Warning!! =>>> Class named " + className + " with scope '" + scope + "' is not exists in apk,  this weave action will be ignored");
                return;
            }
            visitClasses(graph, node, scope, visitor);
        };
    }

    public static NodeVisitor childrenOfInterfaces(SimpleClassGraph graph,
                                                   List<String> interfaces,
                                                   Scope scope) {
        return visitor -> {
            for (String interfaceName : interfaces) {
                ClassNode node = graph.get(interfaceName);
                if (node == null) {
                    WeaverLog.e("Weaver Warning!! =>>> Interface named " + interfaceName + " with scope '" + scope + "' is not exists in apk,  this weave action will be ignored");
                    continue;
                }
                visitInterfaceImplementations(graph, interfaceName, scope, visitor);
            }
        };
    }

    private static void visitInterfaceImplementations(SimpleClassGraph graph, String interfaceName, Scope scope, Consumer<ClassNode> visitor) {
        List<ClassNode> implementations = graph.getImplementations(interfaceName);
        for (ClassNode impl : implementations) {
            switch (scope) {
                case SELF:
                    visitor.accept(impl);
                    break;
                case ALL:
                case ALL_CHILDREN:
                    visitor.accept(impl);
                    visitClasses(graph, impl, scope, visitor);
                    break;
                case DIRECT:
                    visitor.accept(impl);
                    break;
                case LEAF:
                    List<ClassNode> children = graph.getChildren(impl.name);
                    if (children.isEmpty()) {
                        visitor.accept(impl);
                    } else {
                        visitClasses(graph, impl, scope, visitor);
                    }
                    break;
            }
        }
    }

    private static void visitClasses(SimpleClassGraph graph, ClassNode classNode, Scope scope, Consumer<ClassNode> visitor) {
        List<ClassNode> children = graph.getChildren(classNode.name);
        switch (scope) {
            case SELF:
                visitor.accept(classNode);
                break;
            case ALL:
                visitor.accept(classNode);
                for (ClassNode child : children) {
                    visitClasses(graph, child, scope, visitor);
                }
                break;
            case ALL_CHILDREN:
                for (ClassNode child : children) {
                    visitClasses(graph, child, scope, visitor);
                }
                break;
            case DIRECT:
                for (ClassNode child : children) {
                    visitor.accept(child);
                }
                break;
            case LEAF:
                if (children.isEmpty()) {
                    visitor.accept(classNode);
                } else {
                    for (ClassNode child : children) {
                        visitClasses(graph, child, scope, visitor);
                    }
                }
                break;
        }
    }
}
