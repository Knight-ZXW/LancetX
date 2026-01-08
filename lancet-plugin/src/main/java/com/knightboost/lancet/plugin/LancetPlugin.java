package com.knightboost.lancet.plugin;

import com.android.build.gradle.AppExtension;
import com.knightboost.lancet.internal.log.WeaverLog;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LancetPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // 创建扩展
        LancetExtension extension = project.getExtensions().create("LancetX", LancetExtension.class, project);
        LancetExtension.sLancetExtension = extension;

        // 注册 Transform
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(new LancetTransform(project, extension));

        WeaverLog.i("LancetX plugin applied successfully");
    }
}
