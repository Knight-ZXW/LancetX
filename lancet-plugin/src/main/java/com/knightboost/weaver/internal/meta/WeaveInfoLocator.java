package com.knightboost.weaver.internal.meta;

import com.google.common.collect.Sets;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.internal.entity.BeforeCallInfo;
import com.knightboost.weaver.internal.entity.InsertInfo;
import com.knightboost.weaver.internal.entity.ProxyInfo;
import com.knightboost.weaver.internal.entity.ReplaceInfo;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.knightboost.weaver.internal.entity.TryCatchInfo;
import com.knightboost.weaver.internal.exception.IllegalAnnotationException;
import com.knightboost.weaver.internal.graph.GraphUtil;
import com.knightboost.weaver.internal.log.WeaverLog;
import com.knightboost.weaver.internal.parser.AopMethodAdjuster;
import com.knightboost.weaver.internal.util.TypeUtils;
import com.ss.android.ugc.bytex.common.graph.ClassNode;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.graph.InterfaceNode;
import com.ss.android.ugc.bytex.common.graph.MethodEntity;
import com.ss.android.ugc.bytex.common.graph.Node;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Set;

/**
 * Weave函数 信息收集器
 * <p>
 * Created by Knight-ZXW on 17/5/3.
 */
public class WeaveInfoLocator {

    //flag type
    //0x01
    private static final int INSERT = 0x01;
    private static final int PROXY = 0x10;
    private static final int TRY_CATCH = 0x100;
    private static final int REPLACE = 0x1000;

    private static final int PUBLIC_STATIC = Opcodes.ACC_STATIC
            | Opcodes.ACC_PUBLIC;

    private int flag = 0;

    // 需要做字节码修改的目标类, 因为存在正则匹配，所以这里是个集合
    //className Set of targetClasses
    private Set<String> targetClasses;
    private Set<String> tempClasses;

    //目标函数的签名
    private String targetMethodDesc;
    //目标函数的名称
    private String targetMethodName;
    private boolean isStatic;

    private boolean mayCreateSuper;
    private String nameRegex;
    private boolean matchResultReverse;

    private Type[] argsType;
    private Type returnType;

    //replace info
    private String replaceClassName;
    private String replaceMethodName;
    private String replaceMethodDesc;

    //weaver 函数的信息
    private MethodNode sourceNode;
    //weaver类名称
    private String sourceClass;

    private final Graph graph;

    public WeaveInfoLocator(Graph graph) {
        this.graph = graph;
    }

    public Graph graph() {
        return graph;
    }

    public Type[] getArgsType() {
        return argsType;
    }

    public void goMethod() {
        tempClasses = null;
    }

    public void intersectTargetClasses(Set<String> targetClasses) {

        if (tempClasses == null) {
            this.tempClasses = targetClasses;
        } else {
            tempClasses = Sets.intersection(tempClasses, targetClasses);
        }
        this.targetClasses = tempClasses;
    }

    public void adjustTargetMethodArgs(int index, Type type) {
        argsType[index] = type;
        //todo 是否需要还原
        targetMethodDesc = sourceNode.desc =
                Type.getMethodDescriptor(returnType, argsType);
    }

    public void setInsert(String targetMethod,
                          boolean mayCreateSuper) {
        this.flag |= INSERT;
        this.targetMethodName = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
    }


    public void setReplace(
            String targetMethodName,
            String targetMethodDesc,
            String replaceClassName,
            String replaceMethodName,
            String replaceMethodDesc,
            boolean isStatic) {
        this.flag |= REPLACE;
        this.targetMethodDesc = targetMethodDesc;
        this.targetMethodName = targetMethodName;

        this.replaceClassName = replaceClassName;
        this.replaceMethodName = replaceMethodName;
        this.replaceMethodDesc = replaceMethodDesc;
        this.isStatic = isStatic;
    }

    public void setProxy(String targetMethod) {
        this.flag |= PROXY;
        this.targetMethodName = targetMethod;
    }

    public void setTryCatch() {
        this.flag |= TRY_CATCH;
    }

    public void setNameRegex(String regex,boolean reverse) {
        this.nameRegex = regex;
        this.matchResultReverse = reverse;
    }

    public void setSourceNode(String sourceClass, MethodNode node) {
        this.sourceClass = sourceClass;
        this.sourceNode = node;

        //targetMethodDesc 默认和 sourceNode.desc 一样
        targetMethodDesc = sourceNode.desc;

        argsType = Type.getArgumentTypes(targetMethodDesc);
        returnType = Type.getReturnType(targetMethodDesc);
    }

    public void appendTo(TransformInfo transformInfo) {
        check();
        switch (flag) {
            case INSERT:
                for (String targetClass : targetClasses) {
                    if (mayCreateSuper){
                        MethodEntity finalOriginalMethod = GraphUtil.findFinalOriginalMethod(graph,
                                targetClass, targetMethodName, targetMethodDesc);
                        //跳过函数是final的
                        if (finalOriginalMethod!=null && !finalOriginalMethod.className().equals(targetClass)){
                            WeaverLog.e(" >> << ignore Replace for final "
                            +targetClass+" "+targetMethodName);
                            continue;
                        }
                    }
                    InsertInfo insertInfo = new InsertInfo(mayCreateSuper,
                            targetClass, targetMethodName, targetMethodDesc,
                            sourceClass, sourceNode);
                    transformInfo.addInsertInfo(insertInfo);
                }
                break;
            case PROXY:
                for (String c : targetClasses) {
                    ProxyInfo proxyInfo = new ProxyInfo(nameRegex, c, targetMethodName, targetMethodDesc, sourceClass, sourceNode);
                    transformInfo.addProxyInfo(proxyInfo);
                }
                break;
            case REPLACE:
                generateAndAddReplaceInfo(transformInfo);
                break;
            case TRY_CATCH:
                transformInfo.addTryCatch(new TryCatchInfo(nameRegex, sourceClass, sourceNode.name, targetMethodDesc));
                break;
        }
    }

    private void generateAndAddReplaceInfo(TransformInfo transformInfo) {
        targetClasses.stream()
                .map(c -> {

                    ReplaceInfo replaceInfo = new ReplaceInfo(
                            nameRegex, c,
                            targetMethodName,
                            targetMethodDesc,
                            sourceClass,
                            sourceNode);

                    replaceInfo.matchResultReverse =this.matchResultReverse;

                    replaceInfo.replaceClassName = this.replaceClassName;
                    replaceInfo.replaceMethodName = this.replaceMethodName;
                    replaceInfo.replaceMethodDesc = this.replaceMethodDesc;
                    replaceInfo.targetIsStatic = this.isStatic;
                    replaceInfo.check();
                    return replaceInfo;
                })
                .forEach(transformInfo::addReplaceInfo);
    }



    private void check() {
        if (flag <= 0) {
            throw new IllegalAnnotationException("no @Proxy, @Insert or @TryCatchHandler on " + sourceClass + "." + sourceNode.name);
        } else if (Integer.bitCount(flag) > 1) {

            //todo 某些注解 不允许一起出现
            throw new IllegalAnnotationException("@Proxy @Insert or @TryCatchHandler can only appear once");
        }
        if (flag != TRY_CATCH) {
            if (targetClasses == null) {
                throw new IllegalAnnotationException("no @targetClass or @ImplementedInterface on " + sourceClass + "." + sourceNode.name);
            }
            if (targetClasses.size() <= 0) {
                WeaverLog.w("can't find satisfied class with " + sourceClass + "." + sourceNode.name);
            }
        } else {
            if (!targetMethodDesc.equals("(Ljava/lang/Throwable;)Ljava/lang/Throwable;") ||
                    (sourceNode.access & PUBLIC_STATIC) != PUBLIC_STATIC) {
                throw new IllegalAnnotationException("method annotated with @TryCatchHandler should be like this: " +
                        "public static Throwable method_name(Throwable)");
            }
        }
        if (mayCreateSuper && TypeUtils.isStatic(sourceNode.access)) {
            throw new IllegalAnnotationException("can't use mayCreateSuper while method is static, " + sourceClass + "." + sourceNode.name);
        }
    }

    /**
     * 是否满足 字节码 代理替换的操作
     *
     * @return
     */
    public boolean satisfied() {
        return targetClasses != null && targetClasses.size() > 0 && (flag == INSERT || flag == PROXY);
    }

    public void transformNode() {
        new AopMethodAdjuster(
                flag == INSERT,
                sourceClass,
                sourceNode)
                .adjust();
    }



    @Override
    public String toString() {
        return "HookInfoLocator{" +
                "flag=" + flag +
                ", classes=" + targetClasses +
                ", targetDesc='" + targetMethodDesc + '\'' +
                ", targetMethod='" + targetMethodName + '\'' +
                ", mayCreateSuper=" + mayCreateSuper +
                ", nameRegex='" + nameRegex + '\'' +
                ", argsType=" + Arrays.toString(argsType) +
                ", returnType=" + returnType +
                ", sourceClass='" + sourceClass + '\'' +
                '}';
    }
}
