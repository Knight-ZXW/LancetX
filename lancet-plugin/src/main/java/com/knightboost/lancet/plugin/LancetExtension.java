
package com.knightboost.lancet.plugin;

import com.knightboost.lancet.internal.log.WeaverLog;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;


public class LancetExtension {

    protected static LancetExtension sLancetExtension;

    public static final LancetExtension instance() {
        return sLancetExtension;
    }


    private NamedDomainObjectContainer<WeaveGroup> weaveGroup;
    private boolean enable = false;
    private boolean enableInDebug = false;

    public LancetExtension(Project project) {
        bindProject(project);
    }


    public void bindProject(Project project) {
        weaveGroup = project.getObjects()
                .domainObjectContainer(WeaveGroup.class);
    }


    public void weaveGroup(Action<? super NamedDomainObjectContainer<WeaveGroup>> action) {
        action.execute(weaveGroup);
    }

    public WeaveGroup findWeaveGroup(String group) {
        return weaveGroup.findByName(group);
    }

    public boolean isWeaveGroupEnable(String group){
        if (group ==null){ // 没有使用@WeaveGroup 注解，则功能开关 依赖于 全局的插件开关
            return isEnable();
        }

        WeaveGroup weaveGroup = findWeaveGroup(group);
        if (weaveGroup == null){
            // 如果 配置了@Group 注解，但是 没有配置对应的 extension,则默认将该功能关闭
            // todo  ，允许在 注解上配置默认的功能开关
            WeaverLog.i("未发现 weaver group "+group+" 的gradle 配置 ,因此功能默认关闭");
            return false;
        }
        return weaveGroup.isEnable();
    }

    public boolean isEnable() {
        return enable;
    }

    public void enable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnableInDebug() {
        return enableInDebug;
    }

    public void enableInDebug(boolean enableInDebug) {
        this.enableInDebug = enableInDebug;
    }

    public String getName() {
        return "LancetX";
    }
}