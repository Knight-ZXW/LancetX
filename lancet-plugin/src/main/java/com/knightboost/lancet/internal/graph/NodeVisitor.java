package com.knightboost.lancet.internal.graph;


import com.ss.android.ugc.bytex.common.graph.Node;

import java.util.function.Consumer;

public interface NodeVisitor {
    void forEach(Consumer<Node> node);

}
