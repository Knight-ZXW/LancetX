package com.knightboost.weaver.plugin;

/**
 * weave 插桩 功能组 及其开关
 */
public class WeaveGroup {

    private String name;

    public WeaveGroup(String name) {
        this.name = name;
    }

    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void enable(boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
