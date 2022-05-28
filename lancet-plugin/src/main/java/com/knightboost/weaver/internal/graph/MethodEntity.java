package com.knightboost.weaver.internal.graph;

/**
 * Created by Knight-ZXW on 17/5/11.
 */
public class MethodEntity {

    public int access;
    public String name;
    public String desc;

    public MethodEntity(int access, String name, String desc) {
        this.access = access;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "MethodEntity{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
