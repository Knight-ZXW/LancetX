package com.knightboost.weaver.internal.asm.classvisitor;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.knightboost.weaver.plugin.KnightWeaveContext;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.visitor.ClassVisitorChain;
import com.ss.android.ugc.bytex.transformer.TransformContext;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class WeaveTransformer {

    public static final String AID_INNER_CLASS_NAME = "_boostWeave";

    // simple name of innerClass
    Map<String, ClassWriter> mInnerClassWriter = new HashMap<>();

    public String originClassName;
    public String superName;

    public BaseWeaveClassVisitor headVisitor;
    public BaseWeaveClassVisitor tailVisitor;

    //same as headVisitor
    public ClassVisitor originalClassVisitor;

    private final ClassVisitorChain chain;
    private final Graph graph;

    public final MethodChain methodChain;

    public WeaveTransformer(ClassVisitorChain visitorChain, Graph graph) {
        this.chain = visitorChain;
        this.graph = graph;
        TransformInfo transformInfo = KnightWeaveContext.instance().getTransformInfo();

        methodChain = new MethodChain(KnightWeaveContext.instance().getClassGraph());

        HookClassVisitor hookClassVisitor = new HookClassVisitor(transformInfo.hookClasses);
        connect(hookClassVisitor);
//        transform.connect(new BeforeCallClassVisitor(transformInfo.beforeCallInfo));
        connect(new InsertClassVisitor(transformInfo.insertInfo));
        connect(new ProxyClassVisitor(transformInfo.proxyInfo));
        connect(new ReplaceClassVisitor(transformInfo));
    }

    public Graph getGraph(){
        return graph;
    }


    private void connect(BaseWeaveClassVisitor visitor) {
        chain.connect(visitor);
        visitor.transformer = this;
        if (headVisitor == null) {
            this.originalClassVisitor = visitor;
            headVisitor = visitor;
            tailVisitor = visitor;
        } else {
            tailVisitor = visitor;
        }

    }

    public ClassVisitor getInnerClassVisitor(String classSimpleName) {
        ClassWriter writer = mInnerClassWriter.get(classSimpleName);
        if (writer == null) {
            writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            initForWriter(writer, classSimpleName);
            mInnerClassWriter.put(classSimpleName, writer);
        }
        return writer;
    }


    public String getCanonicalName(String simpleName) {
        return originClassName + "$" + simpleName;
    }

    private void initForWriter(ClassVisitor visitor, String classSimpleName) {
        visitor.visit(Opcodes.V1_7,
                Opcodes.ACC_SUPER,
                getCanonicalName(classSimpleName),
                null,
                "java/lang/Object",
                null);
        MethodVisitor mv = visitor.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }


    public void generateInnerClasses() {
        for (String className : mInnerClassWriter.keySet()) {
            tailVisitor.visitInnerClass(getCanonicalName(className), originClassName, className, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
        mInnerClassWriter.forEach(new BiConsumer<String, ClassWriter>() {
            @Override
            public void accept(String s, ClassWriter classWriter) {
                byte[] bytes = classWriter.toByteArray();
                TransformContext transformContext = KnightWeaveContext.instance()
                        .getTransformContext();
                try {
                    File dest = transformContext.getInvocation()
                            .getOutputProvider()
                            .getContentLocation("weaveInner", TransformManager.CONTENT_CLASS,
                                    ImmutableSet.of(QualifiedContent.Scope.PROJECT),
                                    Format.DIRECTORY);
                    File classFile = new File(dest, originClassName + "$" + s + ".class");
                    classFile.getParentFile().mkdirs();

                    new FileOutputStream(new File(dest,originClassName+"$"+s+".class")).write(bytes);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}
