package com.knightboost.lancet.internal.asm.classvisitor;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.knightboost.lancet.internal.entity.TransformInfo;
import com.knightboost.lancet.plugin.LancetContext;
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

    /**
     * 当前类的类名
     */
    public String className;
    public String superName;

    public BaseWeaveClassVisitor headVisitor;
    public BaseWeaveClassVisitor tailVisitor;

    //same as headVisitor
    public ClassVisitor originalClassVisitor;

    private ClassVisitorChain chain;
    private final Graph graph;

    public final MethodChain methodChain;

    public WeaveTransformer( Graph graph) {
        this.graph = graph;
        methodChain = new MethodChain(LancetContext.instance().getClassGraph());
    }

    public void initVisitorChain(ClassVisitorChain visitorChain){
        this.chain = visitorChain;
        OriginalClassVisitor originalClassVisitor = new OriginalClassVisitor();
        TransformInfo transformInfo = LancetContext.instance().getTransformInfo();
        connect(new HookClassVisitor(transformInfo,originalClassVisitor));
//        transform.connect(new BeforeCallClassVisitor(transformInfo.beforeCallInfo));
        connect(new InsertClassVisitor(transformInfo.insertInfo));
        connect(new ProxyClassVisitor(transformInfo.proxyInfo));
        connect(new ReplaceClassVisitor(transformInfo));
        connect(new TryCatchClassVisitor(transformInfo));
        connect(originalClassVisitor);
        this.originalClassVisitor = originalClassVisitor;

    }

    public Graph getGraph(){
        return graph;
    }

    public MethodChain getMethodChain(){
        return methodChain;
    }


    private void connect(BaseWeaveClassVisitor visitor) {
        chain.connect(visitor);
        visitor.transformer = this;
        if (headVisitor == null) {
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
        return className + "$" + simpleName;
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
            tailVisitor.visitInnerClass(getCanonicalName(className), this.className, className, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
        mInnerClassWriter.forEach(new BiConsumer<String, ClassWriter>() {
            @Override
            public void accept(String s, ClassWriter classWriter) {
                byte[] bytes = classWriter.toByteArray();
                TransformContext transformContext = LancetContext.instance()
                        .getTransformContext();
                try {
                    File dest = transformContext.getInvocation()
                            .getOutputProvider()
                            .getContentLocation("weaveInner", TransformManager.CONTENT_CLASS,
                                    ImmutableSet.of(QualifiedContent.Scope.PROJECT),
                                    Format.DIRECTORY);
                    File classFile = new File(dest, className + "$" + s + ".class");
                    classFile.getParentFile().mkdirs();

                    new FileOutputStream(new File(dest, className +"$"+s+".class")).write(bytes);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}
