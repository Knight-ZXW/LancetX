package com.knightboost.lancet.plugin;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformInput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.knightboost.lancet.internal.asm.visitor.WeaveTransformer;
import com.knightboost.lancet.internal.graph.SimpleClassGraph;
import com.knightboost.lancet.internal.parser.WeaverClassesParser;
import com.knightboost.lancet.api.annotations.Weaver;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

public class LancetTransform extends Transform {

    private final Project project;
    private final LancetExtension extension;
    private WeaverClassesParser weaverClassesParser;
    private SimpleClassGraph classGraph;

    public LancetTransform(Project project, LancetExtension extension) {
        this.project = project;
        this.extension = extension;
        this.weaverClassesParser = new WeaverClassesParser();
    }

    private void initContext() {
        if (LancetContext.instance() == null) {
            AppExtension android = project.getExtensions().getByType(AppExtension.class);
            LancetContext context = new LancetContext(project, android, extension);
            LancetContext.setInstance(context);
        }
    }

    @Override
    public String getName() {
        return "LancetX";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(@Nonnull TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        // 初始化上下文
        initContext();

        if (!extension.isEnable()) {
            System.out.println("LancetX: Plugin is disabled");
            return;
        }

        System.out.println("LancetX: Starting transformation...");

        // 阶段0：构建类图
        classGraph = new SimpleClassGraph();
        buildClassGraph(transformInvocation, classGraph);
        System.out.println("LancetX: Built class graph with " + classGraph.getClass().getName() + " nodes");

        // 阶段1：扫描所有类，查找 Weaver 类
        weaverClassesParser = new WeaverClassesParser();
        weaverClassesParser.graph = classGraph;
        scanForWeaverClasses(transformInvocation);
        System.out.println("LancetX: Found " + weaverClassesParser.weaverClasses.size() + " weaver classes");

        // 阶段2：解析 Weaver 类
        weaverClassesParser.parse();
        System.out.println("LancetX: Parsed weaver classes");

        // 阶段3：执行字节码转换
        transformClasses(transformInvocation);
        System.out.println("LancetX: Transformation completed");
    }

    private void buildClassGraph(TransformInvocation invocation, SimpleClassGraph classGraph) throws IOException {
        for (TransformInput input : invocation.getInputs()) {
            for (QualifiedContent directoryInput : input.getDirectoryInputs()) {
                buildGraphFromDirectory(directoryInput.getFile(), classGraph);
            }
            for (QualifiedContent jarInput : input.getJarInputs()) {
                buildGraphFromJar(jarInput.getFile(), classGraph);
            }
        }
    }

    private void buildGraphFromDirectory(File directory, SimpleClassGraph classGraph) throws IOException {
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                directory, new String[]{"class"}, true);

        for (File file : files) {
            try (InputStream is = new FileInputStream(file)) {
                ClassReader classReader = new ClassReader(is);
                ClassNode classNode = new ClassNode(Opcodes.ASM9);
                classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                classGraph.addClass(classNode);
            }
        }
    }

    private void buildGraphFromJar(File jarFile, SimpleClassGraph classGraph) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        ClassReader classReader = new ClassReader(is);
                        ClassNode classNode = new ClassNode(Opcodes.ASM9);
                        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                        classGraph.addClass(classNode);
                    }
                }
            }
        }
    }

    private void scanForWeaverClasses(TransformInvocation invocation) throws IOException {
        for (TransformInput input : invocation.getInputs()) {
            for (QualifiedContent directoryInput : input.getDirectoryInputs()) {
                scanDirectory(directoryInput.getFile());
            }
            for (QualifiedContent jarInput : input.getJarInputs()) {
                scanJar(jarInput.getFile());
            }
        }
    }

    private void scanDirectory(File directory) throws IOException {
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                directory, new String[]{"class"}, true);

        for (File file : files) {
            try (InputStream is = new FileInputStream(file)) {
                ClassReader classReader = new ClassReader(is);
                ClassNode classNode = new ClassNode(Opcodes.ASM9);
                classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                if (isWeaverClass(classNode)) {
                    weaverClassesParser.addWeaverClass(classNode);
                }
            }
        }
    }

    private void scanJar(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        ClassReader classReader = new ClassReader(is);
                        ClassNode classNode = new ClassNode(Opcodes.ASM9);
                        classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                        if (isWeaverClass(classNode)) {
                            weaverClassesParser.addWeaverClass(classNode);
                        }
                    }
                }
            }
        }
    }

    private boolean isWeaverClass(ClassNode classNode) {
        if (classNode.visibleAnnotations == null) {
            return false;
        }
        for (AnnotationNode annotation : classNode.visibleAnnotations) {
            if (annotation.desc.equals(Type.getDescriptor(Weaver.class))) {
                return true;
            }
        }
        return false;
    }

    private void transformClasses(TransformInvocation invocation) throws IOException {
        WeaveTransformer weaveTransformer = new WeaveTransformer();

        for (TransformInput input : invocation.getInputs()) {
            // 处理目录输入
            for (QualifiedContent directoryInput : input.getDirectoryInputs()) {
                transformDirectory(directoryInput.getFile(), invocation.getOutputProvider(), weaveTransformer);
            }

            // 处理 JAR 输入
            for (QualifiedContent jarInput : input.getJarInputs()) {
                transformJar(jarInput.getFile(), invocation.getOutputProvider(), weaveTransformer);
            }
        }
    }

    private void transformDirectory(File inputDir, com.android.build.api.transform.TransformOutputProvider outputProvider,
                                     WeaveTransformer weaveTransformer) throws IOException {
        // 获取输出目录
        File outputDir = outputProvider.getContentLocation(
                "lancet_" + inputDir.getName() + "_" + System.currentTimeMillis(),
                getInputTypes(), getScopes(), Format.DIRECTORY);
        outputDir.mkdirs();

        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                inputDir, new String[]{"class"}, true);

        for (File file : files) {
            File outputFile = new File(outputDir, file.getName());

            try (FileInputStream fis = new FileInputStream(file);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                transformClass(fis, fos, weaveTransformer);
            }
        }
    }

    private void transformJar(File inputJar, com.android.build.api.transform.TransformOutputProvider outputProvider,
                               WeaveTransformer weaveTransformer) throws IOException {
        // 获取输出 JAR 位置
        File outputJar = outputProvider.getContentLocation(
                "lancet_" + inputJar.getName() + "_" + System.currentTimeMillis(),
                getInputTypes(), getScopes(), Format.JAR);

        System.out.println("LancetX: Transforming JAR " + inputJar + " -> " + outputJar);

        File tempJar = File.createTempFile("lancet_transform_" + System.currentTimeMillis(), ".jar");
        tempJar.deleteOnExit();

        try (JarFile jar = new JarFile(inputJar);
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar))) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                jos.putNextEntry(new ZipEntry(entry.getName()));

                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        transformClass(is, jos, weaveTransformer);
                    }
                } else {
                    // 复制非 class 文件
                    try (InputStream is = jar.getInputStream(entry)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            jos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                jos.closeEntry();
            }
        }

        // 确保输出目录存在
        outputJar.getParentFile().mkdirs();

        // 删除旧文件（如果存在）
        if (outputJar.exists()) {
            outputJar.delete();
        }

        // 复制到最终位置
        org.apache.commons.io.FileUtils.copyFile(tempJar, outputJar);
        System.out.println("LancetX: Successfully transformed JAR to " + outputJar);
    }

    private void transformClass(InputStream input, OutputStream output, WeaveTransformer weaveTransformer) throws IOException {
        ClassReader classReader = new ClassReader(input);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        // 创建访问者链
        ClassVisitor chain = weaveTransformer.createVisitorChain(classWriter);

        // 执行转换
        classReader.accept(chain, ClassReader.EXPAND_FRAMES);

        // 输出转换后的字节码
        byte[] transformedBytes = classWriter.toByteArray();
        output.write(transformedBytes);
    }

    private String getClassName(File base, File file) {
        String path = file.getAbsolutePath();
        String basePath = base.getAbsolutePath();
        return path.substring(basePath.length() + 1, path.length() - 6)
                .replace(File.separatorChar, '.');
    }

    /**
     * 通过反射获取 Format 枚举值
     */
    private Object getFormat(String formatName) throws Exception {
        Class<?> formatClass = Class.forName("com.android.build.api.transform.QualifiedContent$Format");
        return formatClass.getField(formatName).get(null);
    }
}
