package com.knightboost.lancet.internal.entity;

import com.google.common.base.Strings;
import com.knightboost.lancet.internal.util.AsmUtil;
import com.knightboost.lancet.internal.util.TypeUtils;

import org.objectweb.asm.tree.MethodNode;

import java.util.regex.Pattern;


/**
 * todo:
 * 1.是否需要保证Replace只能有一处
 * 2.如果对相同的目标有多个Replace逻辑，如何保证顺序
 * Created by Knight-ZXW on 17/3/27.
 */
public class ReplaceInfo {

    public String regex;
    public String targetClassName;
    public String targetMethodName;
    public String targetMethodDesc;



    public String replaceClassName;
    public String replaceMethodName;
    public String replaceMethodDesc;

    public String sourceClass;
    public MethodNode sourceMethod;

    public Pattern pattern;
    public boolean matchResultReverse;

    public boolean targetIsStatic;

    private ThreadLocal<MethodNode> local = new ThreadLocal<MethodNode>() {
        @Override
        synchronized protected MethodNode initialValue() {
            return AsmUtil.clone(sourceMethod);
        }
    };

    public ReplaceInfo(String regex,
                       String targetClassName,
                       String targetMethodName,
                       String targetMethodDesc,
                       String sourceClass, MethodNode sourceMethod) {
        this.regex = regex;
        this.targetClassName = targetClassName;
        this.targetMethodName = targetMethodName;
        this.targetMethodDesc = targetMethodDesc;
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;

        if (!Strings.isNullOrEmpty(regex)) {
            this.pattern = Pattern.compile(regex);
        }
    }

    public void check(){

        if (this.targetMethodName == null){
            this.targetMethodName = this.sourceMethod.name;
        }

        if (!this.targetIsStatic){
            this.targetMethodDesc = TypeUtils.removeFirstParam(this.sourceMethod.desc);
        }

        if (this.replaceClassName == null ||this.replaceClassName.length() ==0){
            this.replaceClassName = this.sourceClass;
        }

        if (this.replaceMethodName ==null || this.replaceMethodName.length() ==0){
            this.replaceMethodName = this.sourceMethod.name;
        }

        this.replaceMethodDesc = this.sourceMethod.desc;

    }

    public MethodNode threadLocalNode() {
        return local.get();
    }

    /**
     *
     * @param className
     * @return
     */
    public boolean match(String className) {
        if (pattern == null)
            return true;

        boolean matches = pattern.matcher(className).matches();
        if (!matchResultReverse){
            return matches;
        }else {
            return !matches;
        }
    }




    @Override
    public String toString() {
        return "ReplaceInfo{" +
                "regex='" + regex + '\'' +
                ", targetClassName='" + targetClassName + '\'' +
                ", targetMethodName='" + targetMethodName + '\'' +
                ", targetMethodDesc='" + targetMethodDesc + '\'' +
                ", replaceClassName='" + replaceClassName + '\'' +
                ", replaceMethodName='" + replaceMethodName + '\'' +
                ", replaceMethodDesc='" + replaceMethodDesc + '\'' +
                ", sourceClass='" + sourceClass + '\'' +
                ", pattern=" + pattern +
                '}';
    }
}
