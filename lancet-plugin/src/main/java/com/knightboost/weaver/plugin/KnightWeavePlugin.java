package com.knightboost.weaver.plugin;

import com.android.build.api.transform.Transform;
import com.android.build.gradle.AppExtension;
import com.knightboost.weaver.api.annotations.Weave;
import com.knightboost.weaver.internal.asm.classvisitor.WeaveTransformer;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.knightboost.weaver.internal.parser.WeaverClassMetaParser;
import com.ss.android.ugc.bytex.common.CommonPlugin;
import com.ss.android.ugc.bytex.common.TransformConfiguration;
import com.ss.android.ugc.bytex.common.flow.TransformFlow;
import com.ss.android.ugc.bytex.common.flow.main.MainTransformFlow;
import com.ss.android.ugc.bytex.common.flow.main.Process;
import com.ss.android.ugc.bytex.common.log.ILogger;
import com.ss.android.ugc.bytex.common.visitor.ClassVisitorChain;
import com.ss.android.ugc.bytex.pluginconfig.anno.PluginConfig;
import com.ss.android.ugc.bytex.transformer.TransformContext;
import com.ss.android.ugc.bytex.transformer.TransformEngine;
import com.ss.android.ugc.bytex.transformer.cache.FileCache;

import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;


@PluginConfig("KnightWeave")
public class KnightWeavePlugin extends CommonPlugin<KnightWeaveExtension, KnightWeaveContext> {

    private ILogger logger;
    private WeaverClassMetaParser weaverClassMetaParser;

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
        logger = context.getLogger();
        KnightWeaveContext.setInstance(context);
        weaverClassMetaParser = new WeaverClassMetaParser();
    }


    @Override
    public void traverse(@Nonnull String relativePath, @Nonnull org.objectweb.asm.tree.ClassNode node) {
        List<AnnotationNode> annotations = node.visibleAnnotations;
        if (annotations != null) {
            for (AnnotationNode annotationNode : annotations) {
                if (annotationNode.desc.equals(Type.getDescriptor(Weave.class))) {
                    //this is weave class
                    weaverClassMetaParser.addWeaverClass(node);
                }
            }
        }

        super.traverse(relativePath, node);
    }

    @Override
    public void beforeTransform(@Nonnull @NotNull TransformEngine engine) {
        super.beforeTransform(engine);
        weaverClassMetaParser.graph = context.getClassGraph();
        weaverClassMetaParser.parse();

        KnightWeaveContext.instance().getLogger()
                .i("transform info \n" + KnightWeaveContext.instance().getTransformInfo());
    }

    @Override
    public boolean transform(@Nonnull String relativePath, @Nonnull ClassVisitorChain chain) {
        WeaveTransformer weaveTransformer = new WeaveTransformer(chain,
                context.getClassGraph());
        return true;
    }


    @Override
    protected TransformFlow provideTransformFlow(@Nonnull MainTransformFlow mainFlow, @Nonnull TransformContext transformContext) {
        return super.provideTransformFlow(mainFlow, transformContext);
    }


    @Override
    protected Transform getTransform() {
        return super.getTransform();
    }

    private ClassLoader createClassLoader(TransformContext context) {
        URL[] urls = Stream.concat(context.getAllJars().stream(),
                context.getAllDirs().stream())
                .map(FileCache::getFile)
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
                        | ClassReader.EXPAND_FRAMES;
            default:
                return 0;
        }
    }

    // @Override
    // public int flagForClassWriter() {
    //     return 0;
    // }
}
