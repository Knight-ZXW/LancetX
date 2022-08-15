package com.knightboost.lancet.internal.graph;
import com.android.tools.r8.w.S;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.internal.log.WeaverLog;
import com.knightboost.lancet.internal.util.TypeUtils;
import com.ss.android.ugc.bytex.common.graph.ClassNode;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.graph.InterfaceNode;
import com.ss.android.ugc.bytex.common.graph.MethodEntity;
import com.ss.android.ugc.bytex.common.graph.Node;

import java.util.List;
import java.util.function.Consumer;

public class GraphUtil {


    public static MethodEntity findFinalOriginalMethod(Graph graph,
                                                       String className,
                                                       String methodName,
                                                       String methodDesc){
        Node node = graph.get(className);
        MethodEntity methodEntity = node.confirmOriginMethod(methodName, methodDesc);
        if (methodEntity!=null && TypeUtils.isFinal(methodEntity.access())){
            return methodEntity;
        }
        return null;
    }

    public static NodeVisitor childrenOf(Graph graph,
                                         String className,
                                         Scope scope) {
        return visitor -> {
            Node node = graph.get(className);
            if (node == null) {
                WeaverLog.e("Weaver Warning!! =>>> Class named " + className + " with scope '" + scope + "' is not exists in apk,  this weave action will be ignored");
                return;
            } else if (!(node instanceof ClassNode)) {
                throw new IllegalArgumentException(className + " is not a class");
            }
            visitClasses((ClassNode) node, scope, visitor);
        };
    }

    public static NodeVisitor childrenOfInterfaces(Graph graph,
                                                   List<String> interfaces,
                                                   Scope scope) {
        return visitor -> {
            for (String interfaceName :interfaces){
                Node node = graph.get(interfaceName);
                if (node == null) {
                    WeaverLog.e("Weaver Warning!! =>>> Class named " + interfaceName + " with scope '" + scope + "' is not exists in apk,  this weave action will be ignored");
                    return;
                }
                if (!(node instanceof com.ss.android.ugc.bytex.common.graph.InterfaceNode)){
                    throw new IllegalStateException(interfaceName+" 不是interface");
                }
                visitImplements((InterfaceNode) node, scope, visitor);
            }

        };
    }

    private static void visitImplements(InterfaceNode node, Scope scope, Consumer<Node> visitor){
        List<ClassNode> classes = node.implementedClasses;
        List<InterfaceNode> children = node.children;
        switch (scope){
            case ALL:
                classes.forEach(c->visitClasses(c,scope,visitor));
                break;
            case DIRECT:
                children.forEach(c->visitImplements(c,scope,visitor));
                break;
            case SELF:
                classes.forEach(visitor);
                break;
            case LEAF:
                children.forEach(c->visitImplements(c,scope,visitor));
                classes.stream()
                        .filter(c->{
                            if (c.children.size() ==0){
                                visitor.accept(c);
                                return false;
                            }
                            return true;
                        }).forEach(c->visitClasses(c,scope,visitor));
        }
    }

    private  static void visitClasses(ClassNode classNode,
                                      Scope scope, Consumer<Node> visitor) {
        List<ClassNode> children = classNode.children;
        switch (scope) {
            case SELF:
                visitor.accept(classNode);
                break;
            case ALL:
                visitor.accept(classNode);
                children.forEach(n -> visitClasses(n, scope, visitor));
            case ALL_CHILDREN:
                children.forEach(n -> visitClasses(n, scope, visitor));
            case DIRECT:
                children.forEach(visitor);
                break;
            case LEAF:
                children.stream()
                        .filter(n -> {
                            if (n.children.size() == 0) {
                                visitor.accept(n);
                                return false;
                            }
                            return true;
                        })
                        .forEach(n -> visitClasses(n, scope, visitor));
                break;
        }
    }
}
