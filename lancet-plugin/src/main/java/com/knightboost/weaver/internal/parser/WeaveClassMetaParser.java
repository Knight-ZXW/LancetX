package com.knightboost.weaver.internal.parser;

import com.google.common.base.Joiner;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.api.annotations.ClassOf;
import com.knightboost.weaver.api.annotations.Group;
import com.knightboost.weaver.api.annotations.ImplementedInterface;
import com.knightboost.weaver.api.annotations.Insert;
import com.knightboost.weaver.api.annotations.NameRegex;
import com.knightboost.weaver.api.annotations.Proxy;
import com.knightboost.weaver.api.annotations.ReplaceInvoke;
import com.knightboost.weaver.api.annotations.TargetClass;
import com.knightboost.weaver.api.annotations.TryCatchHandler;
import com.knightboost.weaver.internal.exception.LoadClassException;
import com.knightboost.weaver.internal.exception.UnsupportedAnnotationException;
import com.knightboost.weaver.internal.graph.GraphUtil;
import com.knightboost.weaver.internal.log.WeaverLog;
import com.knightboost.weaver.internal.meta.ClassMetaInfo;
import com.knightboost.weaver.internal.meta.ClassOfMeta;
import com.knightboost.weaver.internal.meta.ImplementedInterfaceMeta;
import com.knightboost.weaver.internal.meta.InsertAnnoMeta;
import com.knightboost.weaver.internal.meta.MethodMetaInfo;
import com.knightboost.weaver.internal.meta.NameRegexMeta;
import com.knightboost.weaver.internal.meta.ReplaceAnnoMeta;
import com.knightboost.weaver.internal.meta.TargetClassMeta;
import com.knightboost.weaver.internal.meta.TryCatchAnnoMeta;
import com.knightboost.weaver.internal.meta.WeaveInfoLocator;
import com.knightboost.weaver.internal.parser.anno.AcceptAny;
import com.knightboost.weaver.internal.parser.anno.ClassOfAnnoParser;
import com.knightboost.weaver.internal.parser.anno.DelegateAcceptableAnnoParser;
import com.knightboost.weaver.internal.parser.anno.GatheredAcceptableAnnoParser;
import com.knightboost.weaver.internal.parser.anno.ImplementedInterfaceAnnoParser;
import com.knightboost.weaver.internal.parser.anno.InsertAnnoParser;
import com.knightboost.weaver.internal.parser.anno.NameRegexAnnoParser;
import com.knightboost.weaver.internal.parser.anno.ProxyAnnoParser;
import com.knightboost.weaver.internal.parser.anno.ReplaceAnnoParser;
import com.knightboost.weaver.internal.parser.anno.TargetClassAnnoParser;
import com.knightboost.weaver.internal.parser.anno.TryCatchAnnoParser;
import com.knightboost.weaver.internal.util.AsmUtil;
import com.knightboost.weaver.plugin.KnightWeaveContext;
import com.ss.android.ugc.bytex.common.graph.Graph;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Weaver类 元信息类 信息解析
 */
public class WeaveClassMetaParser {

    private AcceptableAnnoParser parser;

    private static final String ANNOTATION_PACKAGE = "L" + ClassOf.class.getPackage()
            .getName().replace(".", "/");


    private static final String GROUP = Type.getDescriptor(Group.class);

    private ClassLoader cl;
    private Graph graph;

    public WeaveClassMetaParser(ClassLoader loader,Graph graph) {
        this.cl = loader;
        this.graph = graph;

        parser = GatheredAcceptableAnnoParser.newInstance(
                new DelegateAcceptableAnnoParser(TargetClass.class, new TargetClassAnnoParser()),
                new DelegateAcceptableAnnoParser(ImplementedInterface.class, new ImplementedInterfaceAnnoParser()),
                new DelegateAcceptableAnnoParser(NameRegex.class, new NameRegexAnnoParser()),
                new DelegateAcceptableAnnoParser(ClassOf.class, new ClassOfAnnoParser()),
                new DelegateAcceptableAnnoParser(Insert.class, new InsertAnnoParser()),
                new DelegateAcceptableAnnoParser(Proxy.class, new ProxyAnnoParser()),
                new DelegateAcceptableAnnoParser(ReplaceInvoke.class, new ReplaceAnnoParser()),
                new DelegateAcceptableAnnoParser(TryCatchHandler.class, new TryCatchAnnoParser()),

                AcceptAny.INSTANCE
        );
    }


    private List<AnnotationMeta> nodesToMetas(List<AnnotationNode> nodes) {
        if (nodes == null || nodes.size() <= 0) {
            return Collections.emptyList();
        }
        return nodes.stream().map(c -> {
            if (!parser.accept(c.desc)) {
                throw new UnsupportedAnnotationException(c.desc + " is not supported");
            }
            return parser.parseAnnotation(c);
        }).collect(Collectors.toList());
    }

    private ClassNode loadClassNode(String className) {
        try {
            URL url = cl.getResource(className + ".class");
            if (url == null) {
                throw new IOException("url == null");
            }
            URLConnection urlConnection = url.openConnection();

            // gradle daemon bug:
            // Different builds in one process because of daemon which makes the jar connection will read the context from cache if they points to the same jar file.
            // But the file may be changed.

            urlConnection.setUseCaches(false);
            ClassReader cr = new ClassReader(urlConnection.getInputStream());
            urlConnection.getInputStream().close();
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_DEBUG);
            checkNode(cn);
            return cn;
        } catch (IOException e) {
            URLClassLoader cl = (URLClassLoader) this.cl;
            throw new LoadClassException("load class failure: " + className + " by\n" + Joiner.on('\n').join(cl.getURLs()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkNode(ClassNode cn) {
        if (cn.fields.size() > 0) {
            String s = ((List<FieldNode>) cn.fields)
                    .stream()
                    .map(fieldNode -> fieldNode.name)
                    .collect(Collectors.joining(","));
            WeaverLog.w("can't declare fields '" + s + "' in hook class " + cn.name);
        }
        int ac = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
        cn.innerClasses.forEach(c -> {
            InnerClassNode n = (InnerClassNode) c;
            if ((n.access & ac) != ac) {
                throw new IllegalStateException("inner class in hook class "
                        + cn.name + " must be public static");
            }
        });
    }

    /**
     * filter method contains "weave" package annotation
     *
     * @param methodNode
     * @return
     */
    private boolean containsWeaveAnnotation(MethodNode methodNode) {
        if (methodNode.visibleAnnotations == null)
            return false;
        return methodNode.visibleAnnotations.stream().anyMatch(annotationNode
                -> annotationNode.desc.startsWith(ANNOTATION_PACKAGE));
    }


    public void parse(List<String> hookClasses) {

        KnightWeaveContext weaveContext = KnightWeaveContext.instance();

        weaveContext.getTransformInfo()
                .setHookClasses(new HashSet<>(hookClasses));

        for (String className : hookClasses) {
            ClassNode cn = loadClassNode(className);
            ClassMetaInfo meta = new ClassMetaInfo(className);

            for (AnnotationNode classAnnotations : cn.visibleAnnotations) {
                if (classAnnotations.desc.equals(GROUP)) {
                    String value = AsmUtil.findAnnotationStringValue(classAnnotations, "value");
                    if (value != null && value.length() > 0) {
                        weaveContext.addGroup(className, value);
                    }
                }
            }
            boolean isEnable = weaveContext.isWeaveClassEnable(className);
            if (!isEnable)
                continue;

            meta.annotationMetas = nodesToMetas(cn.visibleAnnotations);

            meta.methods = ((List<MethodNode>) cn.methods).stream()
                    .filter(this::containsWeaveAnnotation)
                    .map(mn -> {

                        List<AnnotationMeta> methodMetas = nodesToMetas(mn.visibleAnnotations);

                        MethodMetaInfo mm = new MethodMetaInfo(mn);
                        mm.metaList = methodMetas;

                        if (mn.visibleParameterAnnotations != null) {
                            int size = Arrays.stream(mn.visibleParameterAnnotations)
                                    .filter(Objects::nonNull)
                                    .mapToInt(List::size)
                                    .sum() + methodMetas.size();
                            List<AnnotationMeta> paramAnnoMetas = new ArrayList<>(size);
                            for (int i = 0; i < mn.visibleParameterAnnotations.length; i++) {
                                List<AnnotationNode> list = (List<AnnotationNode>) mn.visibleParameterAnnotations[i];
                                if (list != null) {
                                    for (AnnotationNode a : list) {
                                        a.visit(ClassOf.INDEX, i);
                                    }
                                    paramAnnoMetas.addAll(nodesToMetas(list));
                                }
                            }

                            paramAnnoMetas.addAll(methodMetas);
                            mm.metaList = paramAnnoMetas;
                        }

                        return mm;
                    })
                    .filter(Objects::nonNull).collect(Collectors.toList());


            for (MethodMetaInfo method : meta.methods) {
                WeaveInfoLocator weaveInfoLocator
                        = new WeaveInfoLocator(weaveContext.getClassGraph());
                weaveInfoLocator.setSourceNode(className, method.sourceNode);

                List<AnnotationMeta> methodAnnotationMeta = method.metaList;
                for (AnnotationMeta annotationMeta : methodAnnotationMeta) {
                    parseWeaveAnnotationOfMethod(weaveInfoLocator, annotationMeta);
                }

                if (weaveInfoLocator.satisfied()){
                    weaveInfoLocator.transformNode();
                }

                weaveInfoLocator.appendTo(weaveContext.getTransformInfo());


            }

        }

    }

    private void parseWeaveAnnotationOfMethod(WeaveInfoLocator locator, AnnotationMeta meta) {
        if (meta instanceof ClassOfMeta) {
            ClassOfMeta classOfMeta = (ClassOfMeta) meta;
            Graph graph = locator.graph();
            Type origin = locator.getArgsType()[classOfMeta.index];
            int index = classOfMeta.index;
            Type type = classOfMeta.type;

            if (origin.getSort() != Type.OBJECT && origin.getSort() != Type.ARRAY) {
                throw new IllegalArgumentException("@ClassOf 's origin type should be parent in value");
            }
            if (type.getDimensions() == origin.getDimensions()) {
                if (!graph.inherit(classOfMeta.internalClassName(type), classOfMeta.internalClassName(origin))) {
                    throw new IllegalArgumentException("@ClassOf 's origin type should be parent in value");
                }
            } else {
                if (origin.getSort() != Type.OBJECT || !"java/lang/Object".equals(origin.getInternalName())) {
                    throw new IllegalArgumentException("@ClassOf 's origin type should be parent in value");
                }
            }
            //根据weave method的信息
            //自动调整targetMethod的相关数据
            locator.adjustTargetMethodArgs(index, type);
        } else if (meta instanceof ImplementedInterfaceMeta) {

        } else if (meta instanceof NameRegexMeta) {
            NameRegexMeta nameRegexMeta = (NameRegexMeta) meta;
            locator.setNameRegex(nameRegexMeta.regex, nameRegexMeta.revert);
        } else if (meta instanceof InsertAnnoMeta) {
            InsertAnnoMeta insertAnnoMeta = (InsertAnnoMeta) meta;
            locator.setInsert(insertAnnoMeta.targetMethod, insertAnnoMeta.mayCreateSuper);
        } else if (meta instanceof ReplaceAnnoMeta) {
            ReplaceAnnoMeta replaceMeta = (ReplaceAnnoMeta) meta;
            locator.setReplace(
                    replaceMeta.targetMethodName,
                    replaceMeta.targetMethodDesc,
                    replaceMeta.replaceClassName,
                    replaceMeta.replaceMethodName,
                    replaceMeta.replaceMethodDesc,
                    replaceMeta.isStatic
            );

        } else if (meta instanceof TargetClassMeta) {
            TargetClassMeta targetClassMeta = (TargetClassMeta) meta;
            String className = targetClassMeta.className;
            Scope scope = targetClassMeta.scope;
            locator.mayAddCheckFlow(className, scope);
            Set<String> classes = new HashSet<>();
            GraphUtil.childrenOf(locator.graph(), className, scope)
                    .forEach(node -> {
                        try {
                            classes.add(node.entity.name);
                        }catch (Exception e){
                            System.out.println("node is "+node+" entity is "+node.entity);
                            throw  e;
                        }

                    });

            WeaverLog.tag(TargetClassAnnoParser.class)
                    .i(scope.name() + " scope of " + className + ", target classes contains = > " + classes);

            locator.intersectTargetClasses(classes);
        } else if (meta instanceof TryCatchAnnoMeta) {

        }

    }

}
