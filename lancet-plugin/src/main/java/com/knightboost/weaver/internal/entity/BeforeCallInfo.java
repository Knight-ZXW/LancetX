package com.knightboost.weaver.internal.entity;

import com.google.common.base.Strings;
import com.knightboost.weaver.internal.util.AsmUtil;
import com.knightboost.weaver.internal.util.TypeUtils;

import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BeforeCallInfo {

    public Pattern pattern;
    public String regex;
    public String targetClassName;
    public String targetMethodName;
    public String targetMethodDesc;

    public String callClassName;
    public String callMethodName;
    public String callMethodDesc;

    public String sourceClass;
    public MethodNode sourceMethod;

    private List<Integer> allowInvokeType = new ArrayList<Integer>(4);

    private ThreadLocal<MethodNode> local = new ThreadLocal<MethodNode>() {
        @Override
        synchronized protected MethodNode initialValue() {
            return AsmUtil.clone(sourceMethod);
        }
    };

    public BeforeCallInfo(String regex,
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

    public void check() {
        //配置默认值
        this.targetMethodName = this.sourceMethod.name;
        this.targetMethodDesc = TypeUtils.removeFirstParam(this.sourceMethod.desc);

        this.callClassName = this.sourceClass;
        this.callMethodName = this.sourceMethod.name;

        this.callMethodDesc = this.sourceMethod.desc;
    }

    public MethodNode threadLocalNode() {
        return local.get();
    }

    /**
     * 检查 class 是否为需要进行字节码修改的类
     * 如果有配置正则表达式，则通过正则表达式判断,
     * 如果没有配置正则表达式，对于replace 替换函数调用的字节码操作来说
     * 只能通过扫描函数内的字节码指令调用来判断，因此默认返回 true
     *
     * @param className
     * @return
     */
    public boolean match(String className) {
        return pattern == null
                || pattern.matcher(className).matches();
    }

    @Override
    public String toString() {
        return "BeforeCallInfo{" +
                "regex='" + regex + '\'' +
                ", targetClassName='" + targetClassName + '\'' +
                ", targetMethodName='" + targetMethodName + '\'' +
                ", targetMethodDesc='" + targetMethodDesc + '\'' +
                ", callClassName='" + callClassName + '\'' +
                ", callMethodName='" + callMethodName + '\'' +
                ", callMethodDesc='" + callMethodDesc + '\'' +
                ", sourceClass='" + sourceClass + '\'' +
                ", sourceMethod=" + sourceMethod +
                ", pattern=" + pattern +
                '}';
    }
}
