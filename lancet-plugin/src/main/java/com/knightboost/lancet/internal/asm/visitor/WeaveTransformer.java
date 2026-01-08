package com.knightboost.lancet.internal.asm.visitor;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.knightboost.lancet.internal.entity.TransformInfo;
import com.knightboost.lancet.plugin.LancetContext;

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

    private LancetClassVisitorChain chain;

    public final MethodChain methodChain;

    public WeaveTransformer() {
        methodChain = new MethodChain(null);
    }

    public void initVisitorChain(LancetClassVisitorChain visitorChain){
        this.chain = new LancetClassVisitorChain();
        BaseWeaveClassVisitor classVisitor =new BaseWeaveClassVisitor();
        TransformInfo transformInfo = LancetContext.instance().getTransformInfo();
        connect(new HookClassVisitor(transformInfo,classVisitor));
        //
        connect(new ChangeClassExtendVisitor(transformInfo));
        //会生成新函数的Visitor
        connect(new InsertClassVisitor(transformInfo.insertInfo));
        connect(new ProxyClassVisitor(transformInfo.proxyInfo));
        connect(classVisitor);
        //不会生成新函数的Visitor
        connect(new ReplaceClassVisitor(transformInfo));
        connect(new ReplaceNewClassVisitor(transformInfo));

        connect(new TryCatchClassVisitor(transformInfo));
        this.originalClassVisitor = classVisitor;

    }

    public ClassVisitor createVisitorChain(ClassVisitor cv) {
        this.chain = new LancetClassVisitorChain();
        BaseWeaveClassVisitor classVisitor = new BaseWeaveClassVisitor();
        TransformInfo transformInfo = LancetContext.instance().getTransformInfo();

        // 构建访问者链
        connect(new HookClassVisitor(transformInfo, classVisitor));
        connect(new ChangeClassExtendVisitor(transformInfo));
        connect(new InsertClassVisitor(transformInfo.insertInfo));
        connect(new ProxyClassVisitor(transformInfo.proxyInfo));
        connect(classVisitor);
        connect(new ReplaceClassVisitor(transformInfo));
        connect(new ReplaceNewClassVisitor(transformInfo));
        connect(new TryCatchClassVisitor(transformInfo));

        // 链接到输出
        this.chain.connect(cv);

        return this.chain.getHead();
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

    public void generateInnerClasses(File outputDir) {
        for (String className : mInnerClassWriter.keySet()) {
            tailVisitor.visitInnerClass(getCanonicalName(className), this.className, className, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
        mInnerClassWriter.forEach(new BiConsumer<String, ClassWriter>() {
            @Override
            public void accept(String s, ClassWriter classWriter) {
                byte[] bytes = classWriter.toByteArray();
                try {
                    File classFile = new File(outputDir, className + "$" + s + ".class");
                    classFile.getParentFile().mkdirs();
                    new FileOutputStream(classFile).write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
