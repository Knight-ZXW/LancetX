package com.knightboost.weaver.plugin;

import com.android.build.gradle.AppExtension;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.ss.android.ugc.bytex.common.BaseContext;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnightWeaveContext extends BaseContext<KnightWeaveExtension> {

    private static KnightWeaveContext sWeaveContext;

    public static final KnightWeaveContext instance() {
        return sWeaveContext;
    }

    // 类名 -> 所属组别  的映射关系
    private final Map<String, String> weaveClassGroupMap = new HashMap<>();

    private final List<String> weaverClasses = new ArrayList<>();

    private final TransformInfo transformInfo =new TransformInfo();


    public static void setInstance(KnightWeaveContext newWeaveContext) {
        sWeaveContext = newWeaveContext;
    }

    public KnightWeaveContext(Project project,
                              AppExtension android,
                              KnightWeaveExtension extension) {
        super(project, android, extension);
    }


    public void addGroup(String weaveClass, String group) {
        weaveClassGroupMap.put(weaveClass, group);
    }

    public boolean isWeaveClassEnable(String weaveClass){
        String group = weaveClassGroupMap.get(weaveClass);
        return extension.isWeaveGroupEnable(group);
    }
    public void addWeaverClasses(String classes) {
        this.weaverClasses.add(classes);
    }


    public TransformInfo getTransformInfo(){
        return transformInfo;
    }

    public void addTransformInfo(TransformInfo transformInfo) {
        this.transformInfo.combine(transformInfo);
    }
}
