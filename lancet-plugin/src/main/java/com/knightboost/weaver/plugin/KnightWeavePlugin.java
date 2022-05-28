package com.knightboost.weaver.plugin;

import com.android.build.api.transform.Transform;
import com.android.build.gradle.AppExtension;
import com.google.common.base.Function;
import com.knightboost.weaver.internal.asm.classvisitor.WeaveTransformer;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.knightboost.weaver.internal.parser.WeaveClassMetaParser;
import com.ss.android.ugc.bytex.common.CommonPlugin;
import com.ss.android.ugc.bytex.common.TransformConfiguration;
import com.ss.android.ugc.bytex.common.flow.TransformFlow;
import com.ss.android.ugc.bytex.common.flow.main.MainTransformFlow;
import com.ss.android.ugc.bytex.common.flow.main.Process;
import com.ss.android.ugc.bytex.common.graph.ClassNode;
import com.ss.android.ugc.bytex.common.graph.Graph;
import com.ss.android.ugc.bytex.common.visitor.ClassVisitorChain;
import com.ss.android.ugc.bytex.pluginconfig.anno.PluginConfig;
import com.ss.android.ugc.bytex.transformer.TransformContext;
import com.ss.android.ugc.bytex.transformer.TransformEngine;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;


@PluginConfig("KnightWeave")
public class KnightWeavePlugin extends CommonPlugin<KnightWeaveExtension, KnightWeaveContext> {

    @Override
    protected KnightWeaveContext getContext(Project project,
                                            AppExtension android,
                                            KnightWeaveExtension extension) {
        KnightWeaveContext knightWeaveContext = new KnightWeaveContext(project, android, extension);
        return knightWeaveContext;
    }

    @Override
    protected void onApply(@Nonnull @NotNull Project project) {
        super.onApply(project);
    }

    @Override
    public void init() {
        super.init();
        KnightWeaveContext.setInstance(context);
    }

    @Override
    public void beforeTransform(@Nonnull @NotNull TransformEngine engine) {
        super.beforeTransform(engine);
        //find all weaveClass
        Graph classGraph = context.getClassGraph();
        ClassLoader cl = getClassLoader(context.getTransformContext());
        List<String> weaveClasses = classGraph
                .childrenOf("com/knightboost/weaver/api/Weaver")
                .stream().map(new Function<ClassNode, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable ClassNode input) {
                        return input.entity.name;
                    }
                }).collect(Collectors.toList());

        WeaveClassMetaParser weaveClassMetaParser = new WeaveClassMetaParser(cl,classGraph);
        weaveClassMetaParser.parse(weaveClasses);

        TransformInfo transformInfo = KnightWeaveContext
                .instance().getTransformInfo();

        KnightWeaveContext.instance().getLogger().i("transform info \n"+transformInfo);

    }

    @Override
    public void startExecute(TransformContext transformContext) {
        super.startExecute(transformContext);
    }

    @Override
    public boolean transform(@Nonnull String relativePath, @Nonnull ClassVisitorChain chain) {
        //找到所有weaver classes

        new WeaveTransformer(chain,context.getClassGraph());
        return super.transform(relativePath, chain);
    }

    @Override
    public boolean transform(@Nonnull String relativePath, @Nonnull org.objectweb.asm.tree.ClassNode node) {
        return super.transform(relativePath, node);
    }

    @Override
    protected TransformFlow provideTransformFlow(@Nonnull MainTransformFlow mainFlow, @Nonnull TransformContext transformContext) {

        return super.provideTransformFlow(mainFlow, transformContext);
    }

    private Transform androidTransform;

    @Override
    protected Transform getTransform() {
        Transform transform = super.getTransform();
        androidTransform = transform;
        return transform;
    }

    private ClassLoader getClassLoader(TransformContext context){
        URL[] urls = Stream.concat(context.getAllJars().stream(),
                context.getAllDirs().stream())
                .map(fileCache -> fileCache.getFile())
                .map(File::toURI)
                .map(u -> {
                    try {
                        return u.toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                })
                .toArray(URL[]::new);
        ClassLoader cl = URLClassLoader.newInstance(urls, null);
        return cl;
    }

    @Nonnull
    @Override
    public TransformConfiguration transformConfiguration() {
        return new TransformConfiguration() {
            @Override
            public boolean isIncremental() {
                return false;
            }
        };
    }

    @Override
    protected KnightWeaveExtension createExtension(Instantiator instantiator,
                                                   Class<KnightWeaveExtension> clazz) {
        KnightWeaveExtension extension = instantiator.newInstance(clazz, project);
        return extension;
    }

    @Override
    public int flagForClassReader(Process process) {
        switch (process) {
            case TRAVERSE:
            case TRAVERSE_ANDROID:
            case TRANSFORM:
                return ClassReader.SKIP_DEBUG
                        | ClassReader.SKIP_FRAMES
                        |ClassReader.EXPAND_FRAMES;
            default:
                return 0;
        }
    }

    // @Override
    // public int flagForClassWriter() {
    //     return 0;
    // }
}
