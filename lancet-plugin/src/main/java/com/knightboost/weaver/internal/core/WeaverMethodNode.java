package com.knightboost.weaver.internal.core;

import com.google.common.base.Strings;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.api.annotations.ClassOf;
import com.knightboost.weaver.api.annotations.Insert;
import com.knightboost.weaver.api.annotations.NameRegex;
import com.knightboost.weaver.api.annotations.Proxy;
import com.knightboost.weaver.api.annotations.ReplaceInvoke;
import com.knightboost.weaver.api.annotations.TargetClass;
import com.knightboost.weaver.api.annotations.TargetMethod;
import com.knightboost.weaver.internal.entity.InsertInfo;
import com.knightboost.weaver.internal.entity.ProxyInfo;
import com.knightboost.weaver.internal.entity.ReplaceInfo;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.knightboost.weaver.internal.exception.IllegalAnnotationException;
import com.knightboost.weaver.internal.graph.GraphUtil;
import com.knightboost.weaver.internal.meta.ClassOfMeta;
import com.knightboost.weaver.internal.parser.AopMethodAdjuster;
import com.knightboost.weaver.internal.util.AnnotationNodeUtil;
import com.knightboost.weaver.internal.util.TypeUtils;
import com.knightboost.weaver.plugin.KnightWeaveContext;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.graph.Node;
import com.ss.android.ugc.bytex.common.log.ILogger;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;


public class WeaverMethodNode {
    private WeaverType weaverType;

    private MethodNode methodNode;
    private ClassNode classNode;
    private Graph graphl;

    private static final String SCOPE_CLASS_DESC = org.objectweb.asm.Type.getDescriptor(Scope.class);

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

    public WeaverMethodNode(Graph graph, ClassNode classNode, MethodNode methodNode) {
        this.graphl = graph;
        this.classNode = classNode;
        this.methodNode = methodNode;
    }

    public String sourceClassName(){
        return  classNode.name;
    }

    public void parseWeaverType(MethodNode methodNode) {
        List<AnnotationNode> invisibleAnnotations = methodNode.invisibleAnnotations;
        List<ParameterNode> parameterNodes = methodNode.parameters;
        if (getAnnotation(methodNode, Insert.class) != null) {
            this.weaverType = WeaverType.INSERT;
        } else if (getAnnotation(methodNode, Proxy.class) != null) {
            this.weaverType = WeaverType.PROXY;
        } else if (getAnnotation(methodNode, ReplaceInvoke.class) != null) {
            this.weaverType = WeaverType.REPLACE_INVOKE;
        }
    }

    public void parseTargetClass(AnnotationNode annotationNode) {
        String targetClassFullName = AnnotationNodeUtil.getAnnotationStringValue(annotationNode, "value");
        if (Strings.isNullOrEmpty(targetClassFullName)) {
            throw new IllegalAnnotationException("@TargetClass value attribute can't be empty or null");
        }
        String scope = AnnotationNodeUtil.getAnnotationStringValue(annotationNode, "scope");
        String[] vs = (String[]) AnnotationNodeUtil.getAnnotationValue(annotationNode, "scope");
        String classNameDescriptor = TypeUtils.classNameToType(targetClassFullName).getDescriptor();
    }

    public void parseTargetMethod(AnnotationNode annotationNode) {
        String methodName = AnnotationNodeUtil.getAnnotationStringValue(annotationNode, "methodName");
    }

    public void parseClassOf(AnnotationNode annotationNode, int parameterIndex) {
        List<Object> values;
        String className = AnnotationNodeUtil.getAnnotationStringValue(annotationNode, ClassOf.INDEX);
        org.objectweb.asm.Type type = TypeUtils.classNameToType(className);
        new ClassOfMeta(parameterIndex, type);
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
                            argumentTypes[index] = type;
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
        if (targetClassNode==null){
            return new ArrayList<>();
        }

        ArrayList<String> targetClasses = new ArrayList<>();

        String targetClassName = AnnotationNodeUtil.getAnnotationStringValue(targetClassNode, "value");
        String[] vs = (String[])AnnotationNodeUtil.getAnnotationValue(targetClassNode,"scope");
        String targetClassDesc = targetClassName.replace('.', '/');

        Scope scope = Scope.SELF;
        if (vs!=null){
            scope =  Scope.valueOf(vs[1]);
        }
        GraphUtil.childrenOf(graphl,targetClassDesc,scope)
                .forEach(new Consumer<Node>() {
                    @Override
                    public void accept(Node node) {
                        targetClasses.add(node.entity.name);
                    }
                });

        return targetClasses;
    }

    public String getTargetMethodName(){
        AnnotationNode targetMethodNode = getAnnotation(methodNode, TargetMethod.class);
        if (targetMethodNode==null){
            return methodNode.name;
        }
        return AnnotationNodeUtil.getAnnotationStringValue(targetMethodNode,"methodName");

    }

    public void build() {
        parseWeaverType(methodNode);
        getLogger().e("weaver type is "+this.weaverType);

        TransformInfo transformInfo = KnightWeaveContext.instance().getTransformInfo();

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

            String targetStaticMethodDesc = TypeUtils.removeFirstParam(targetMethodDesc);

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


    public void parseParameterMeta() {

    }

    public ILogger getLogger(){
        return KnightWeaveContext.instance().getLogger();
    }
}
