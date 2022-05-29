package com.knightboost.weaver.plugin;

import com.android.build.gradle.AppExtension;
import com.knightboost.weaver.internal.entity.TransformInfo;
import com.ss.android.ugc.bytex.common.BaseContext;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class KnightWeaveContext extends BaseContext<KnightWeaveExtension> {

    private static KnightWeaveContext sWeaveContext;

    public static final KnightWeaveContext instance() {
        return sWeaveContext;
    }

    // 类名 -> 所属组别  的映射关系
    private final Map<String, String> weaverClassOfGroupMap = new HashMap<>();

    private final TransformInfo transformInfo =new TransformInfo();

    public static void setInstance(KnightWeaveContext newWeaveContext) {
        sWeaveContext = newWeaveContext;
    }

    public KnightWeaveContext(Project project,
                              AppExtension android,
                              KnightWeaveExtension extension) {
        super(project, android, extension);
    }


    public void registerGroupWeaverClass(String weaverClass, String group) {
        weaverClassOfGroupMap.put(weaverClass, group);
    }

    /**
     * 判断 weaver 功能是否开启
     * @param weaverClass
     * @return
     */
    public boolean isWeaveEnable(String weaverClass){
        String group = weaverClassOfGroupMap.get(weaverClass);
        return extension.isWeaveGroupEnable(group);
    }

    public TransformInfo getTransformInfo(){
        return transformInfo;
    }

}
