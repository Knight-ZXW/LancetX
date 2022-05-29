package com.knightboost.lancet.internal.meta;

import com.knightboost.lancet.internal.parser.AnnotationMeta;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 解析一个Weaver类的 编织信息
 * Created by Knight-ZXW on 17/5/3.
 */
public class ClassMetaInfo {

    public String className;
    public List<AnnotationMeta> annotationMetas = new ArrayList<>();

    public List<MethodMetaInfo> methods = new ArrayList<>(4);

    public ClassMetaInfo(String className) {
        this.className = className;
    }

    // public List<HookInfoLocator> toLocators(Graph nodeMap) {
    //     return methods.stream()
    //             .map(m -> {
    //
    //                 HookInfoLocator locator = new HookInfoLocator(nodeMap);
    //                 locator.setSourceNode(className, m.sourceNode);
    //
    //                 annotationMetas.forEach(i -> {
    //                     i.accept(locator);
    //                 });
    //
    //                 locator.goMethod();
    //
    //                 m.metaList.forEach(i -> {
    //                     i.accept(locator);
    //                 });
    //
    //                 if (locator.satisfied()) {
    //                     locator.transformNode();
    //                 }
    //                 return locator;
    //             }).collect(Collectors.toList());
    // }
}
