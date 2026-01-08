package com.knightboost.lancet.internal.graph;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Consumer;

public interface NodeVisitor {
    void forEach(Consumer<ClassNode> node);
}
