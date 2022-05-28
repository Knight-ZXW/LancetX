package com.knightboost.weaver.api;

/**
 * Created by Knight-ZXW on 21/04/28
 *
 * 目标类的 范围
 *
 */
public enum Scope {
    SELF, // 类自身
    DIRECT, //指定类的所有直接子类
    ALL, // 指定的类 及其所有子类
    ALL_CHILDREN, //指定类的所有子类
    LEAF   //指定类的所有 最终子类
}
