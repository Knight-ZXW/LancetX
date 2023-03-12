package com.knightboost.lancet.internal.entity;

import org.objectweb.asm.tree.MethodNode;

public class ReplaceInvokeInfo {
    private String targetClassType;

    public String getNewClassType() {
        return newClassType;
    }

    private String newClassType;
    private MethodNode methodNode;
    public ReplaceInvokeInfo(String classType, String newClassType,MethodNode methodNode){
        this.targetClassType = classType;
        this.methodNode = methodNode;
        this.newClassType = newClassType;
    };

    public String getTargetClassType() {
        return targetClassType;
    }

    public void setTargetClassType(String targetClassType) {
        this.targetClassType = targetClassType;
    }
}
