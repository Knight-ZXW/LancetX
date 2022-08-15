package com.knightboost.lancet.internal.core;

import com.android.tools.r8.w.S;
import com.knightboost.lancet.api.annotations.ImplementedInterface;
import com.knightboost.lancet.internal.util.AnnotationNodeUtil;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.api.annotations.ClassOf;
import com.knightboost.lancet.api.annotations.Insert;
import com.knightboost.lancet.api.annotations.NameRegex;
import com.knightboost.lancet.api.annotations.Proxy;
import com.knightboost.lancet.api.annotations.ReplaceInvoke;
import com.knightboost.lancet.api.annotations.TargetClass;
import com.knightboost.lancet.api.annotations.TargetMethod;
import com.knightboost.lancet.internal.entity.InsertInfo;
import com.knightboost.lancet.internal.entity.ProxyInfo;
import com.knightboost.lancet.internal.entity.ReplaceInfo;
import com.knightboost.lancet.internal.entity.TransformInfo;
import com.knightboost.lancet.internal.graph.GraphUtil;
import com.knightboost.lancet.internal.parser.AopMethodAdjuster;
import com.knightboost.lancet.internal.util.TypeUtils;
import com.knightboost.lancet.plugin.LancetContext;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.graph.Node;
import com.ss.android.ugc.bytex.common.log.ILogger;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WeaverMethodParser {

    private WeaverType weaverType;
    private final MethodNode methodNode;
    private final ClassNode classNode;
    private final Graph graph;

    private Pattern classNamePattern = Pattern.compile("^(((?![0-9])\\w+\\.)*((?![0-9])\\w+\\$)?(?![0-9])\\w+)((\\[])*)$");

    public static boolean isWeaverMethodNode(MethodNode methodNode){
        List<AnnotationNode> annotationNodes = methodNode.visibleAnnotations;
        if (annotationNodes == null || annotationNodes.size()==0){
            return false;
        }
        for (AnnotationNode annotationNode : annotationNodes) {
            if (annotationNode.desc.equals(Type.getDescriptor(Insert.class))) {
                return true;
            }
            if (annotationNode.desc.equals(Type.getDescriptor(Proxy.class))) {
                return true;
            }
            if (annotationNode.desc.equals(Type.getDescriptor(ReplaceInvoke.class))) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param graph class graph
     * @param classNode weaver classNode
     * @param methodNode weaver method
     */
    public WeaverMethodParser(Graph graph,
                              ClassNode classNode,
                              MethodNode methodNode) {
        this.graph = graph;
        this.classNode = classNode;
        this.methodNode = methodNode;
    }

    public String sourceClassName(){
        return  classNode.name;
    }

    public void parseWeaverType(MethodNode methodNode) {
        if (getAnnotation(methodNode, Insert.class) != null) {
            this.weaverType = WeaverType.INSERT;
        } else if (getAnnotation(methodNode, Proxy.class) != null) {
            this.weaverType = WeaverType.PROXY;
        } else if (getAnnotation(methodNode, ReplaceInvoke.class) != null) {
            this.weaverType = WeaverType.REPLACE_INVOKE;
        }
    }


    public String getTargetMethodDesc() {
        String methodDesc = methodNode.desc;
        List<AnnotationNode>[] visibleParameterAnnotations = methodNode.visibleParameterAnnotations;
        Type[] argumentTypes = Type.getArgumentTypes(methodDesc);

        if (visibleParameterAnnotations != null) {
            for (int index = 0; index < visibleParameterAnnotations.length; index++) {
                List<AnnotationNode> annotationNodes = visibleParameterAnnotations[index];
                if (annotationNodes != null && annotationNodes.size() > 0) {
                    for (AnnotationNode annotationNode : annotationNodes) {
                        if (annotationNode.desc.equals(Type.getDescriptor(ClassOf.class))) {
                            String parameterTypeDesc = AnnotationNodeUtil.getAnnotationStringValue(annotationNode, "value");
                            Type type = TypeUtils.classNameToType(parameterTypeDesc);
                            Type originalType = argumentTypes[index];
                            argumentTypes[index] = type;
                            //检查类型是否合法
                            if (originalType.getSort() != Type.OBJECT && originalType.getSort() != Type.ARRAY) {
                                throw new IllegalArgumentException("@ClassOf 的参数类型应当是对象或者数组类型");
                            }

                            if (type.getDimensions() == originalType.getDimensions()) {
                                if (!graph.inherit(internalClassName(type), internalClassName(originalType))) {
                                    throw new IllegalArgumentException("@ClassOf 's 的参数类型 应当是 @classOf注解值的父类型");
                                }
                            } else {
                                if (originalType.getSort() != Type.OBJECT || !"java/lang/Object".equals(originalType.getInternalName())) {
                                    throw new IllegalArgumentException("@ClassOf 's origin type should be parent in value");
                                }
                            }



                        }
                    }
                }
            }
        }

        StringBuilder methodDescBuilder = new StringBuilder();
        methodDescBuilder.append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            methodDescBuilder.append(argumentTypes[i].getDescriptor());
        }
        methodDescBuilder.append(")");
        methodDescBuilder.append(Type.getReturnType(methodDesc));

        return methodDescBuilder.toString();
    }

    public List<String> getTargetClasses(){
        AnnotationNode targetClassNode = getAnnotation(methodNode, TargetClass.class);
        AnnotationNode implementedInterfaceNode = getAnnotation(methodNode, ImplementedInterface.class);

        if (targetClassNode == null && implementedInterfaceNode == null) {
            //todo throw exception ?
            return new ArrayList<>();
        }

        if (targetClassNode!=null && implementedInterfaceNode!=null){
            throw new IllegalStateException("@TargetClass 和 @ImplementedInterface 不能同时使用");
        }


        ArrayList<String> targetClasses = new ArrayList<>();

        if (targetClassNode!=null){
            String targetClassName = AnnotationNodeUtil.getAnnotationStringValue(targetClassNode, "value");
            String[] vs = (String[])AnnotationNodeUtil.getAnnotationValue(targetClassNode,"scope");
            String targetClassDesc = targetClassName.replace('.', '/');

            Scope scope = Scope.SELF;
            if (vs!=null){
                scope =  Scope.valueOf(vs[1]);
            }
            GraphUtil.childrenOf(graph,targetClassDesc,scope)
                    .forEach(new Consumer<Node>() {
                        @Override
                        public void accept(Node node) {
                            targetClasses.add(node.entity.name);
                        }
                    });
        } else if (implementedInterfaceNode!=null){
            List<String> targetInterfaces = (List<String>) AnnotationNodeUtil.getAnnotationValue(implementedInterfaceNode, "value");
            if (targetInterfaces == null || targetInterfaces.size()==0){
                throw new IllegalArgumentException("@ImplementedInterface values can't be null or empty");
            }
            List<String> targetInterfaceDescList = targetInterfaces.stream().map(new Function<String, String>() {
                @Override
                public String apply(String s) {
                    return s.replace(".", "/");
                }
            }).collect(Collectors.toList());

            String[] vs = (String[])AnnotationNodeUtil.getAnnotationValue(implementedInterfaceNode,"scope");
            Scope scope = Scope.SELF;
            if (vs!=null){
                scope =  Scope.valueOf(vs[1]);
            }
            GraphUtil.childrenOfInterfaces(graph,targetInterfaceDescList,scope)
                    .forEach(new Consumer<Node>() {
                        @Override
                        public void accept(Node node) {
                            targetClasses.add(node.entity.name);
                        }
                    });
        }



        return targetClasses;
    }

    public String getTargetMethodName(){
        AnnotationNode targetMethodNode = getAnnotation(methodNode, TargetMethod.class);
        if (targetMethodNode==null){
            return methodNode.name;
        }
        return AnnotationNodeUtil.getAnnotationStringValue(targetMethodNode,"methodName");

    }

    public void parse() {
        parseWeaverType(methodNode);
        if (this.weaverType == null){
            throw new IllegalStateException(classNode.name+
                    "."+methodNode.name+" 未声明Weave类型");
        }

        TransformInfo transformInfo = LancetContext.instance().getTransformInfo();
        if (this.weaverType ==WeaverType.INSERT){
            AnnotationNode insertAnnotation = getAnnotation(methodNode, Insert.class);
            String targetMethodName = getTargetMethodName();
            String targetMethodDesc = getTargetMethodDesc();

            boolean mayCreateSuper = AnnotationNodeUtil.getAnnotationBoolValue(insertAnnotation, "mayCreateSuper", false);

            List<String> targetClasses = getTargetClasses();

            for (String targetClass : targetClasses) {
                InsertInfo insertInfo = new InsertInfo(mayCreateSuper,
                        targetClass, targetMethodName, targetMethodDesc,
                        sourceClassName(),
                        methodNode
                );
                transformInfo.addInsertInfo(insertInfo);
            }

        }else if (this.weaverType ==WeaverType.PROXY){
            List<String> targetClasses = getTargetClasses();
            String targetMethodName = getTargetMethodName();
            String targetMethodDesc = getTargetMethodDesc();
            AnnotationNode nameRegex = getAnnotation(methodNode, NameRegex.class);
            String regex = null;
            if (nameRegex!=null){
                 regex = AnnotationNodeUtil.getAnnotationStringValue(nameRegex, "value");
            }
            for (String targetClass : targetClasses) {
                ProxyInfo proxyInfo = new ProxyInfo(regex, targetClass, targetMethodName,
                        targetMethodDesc, sourceClassName(), methodNode);
                transformInfo.addProxyInfo(proxyInfo);
            }

        } else  if ( this.weaverType == WeaverType.REPLACE_INVOKE){
            AnnotationNode annotation = getAnnotation(methodNode, ReplaceInvoke.class);
            boolean isStatic = AnnotationNodeUtil.getAnnotationBoolValue(annotation, "isStatic", false);

            List<String> targetClasses = getTargetClasses();
            String targetMethodName = getTargetMethodName();
            String targetMethodDesc = getTargetMethodDesc();

            AnnotationNode nameRegex = getAnnotation(methodNode, NameRegex.class);
            String regex = null;
            if (nameRegex!=null){
                regex = AnnotationNodeUtil.getAnnotationStringValue(nameRegex, "value");
            }

            for (String targetClass : targetClasses) {
                if (!isStatic){
                  targetMethodDesc = TypeUtils.removeFirstParam(targetMethodDesc);
                }
                ReplaceInfo replaceInfo = new ReplaceInfo(regex, targetClass,
                        targetMethodName,
                        targetMethodDesc,
                        sourceClassName(), methodNode);
                replaceInfo.targetIsStatic =isStatic;
                replaceInfo.check();
                transformInfo.addReplaceInfo(replaceInfo);
            }
        }

        new AopMethodAdjuster(weaverType == WeaverType.INSERT,
                sourceClassName(),
                methodNode).adjust();
    }


    private AnnotationNode getAnnotation(MethodNode methodNode, Class annotationClass) {
        List<AnnotationNode> annotations = methodNode.visibleAnnotations;
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (Type.getDescriptor(annotationClass).equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    public void parseMethodAnnotation(AnnotationNode annotationNode) {
        String desc = annotationNode.desc;

        if (Type.getDescriptor(Insert.class).equals(desc)) {
            this.weaverType = WeaverType.INSERT;
        } else if (Type.getDescriptor(Proxy.class).equals(desc)) {
            this.weaverType = WeaverType.PROXY;
        } else if (Type.getDescriptor(ReplaceInvoke.class).equals(desc)) {
            this.weaverType = WeaverType.REPLACE_INVOKE;
        }

    }


    private String internalClassName(Type type) {
        if (type.getSort() == Type.OBJECT) {
            return type.getInternalName();
        } else { // array
            return type.getElementType().getInternalName();
        }
    }


    public ILogger getLogger(){
        return LancetContext.instance().getLogger();
    }
}
