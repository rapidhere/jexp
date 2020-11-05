/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.closure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.var;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * a access property node
 *
 * @author rapid
 * @version : PropertyNode.java, v 0.1 2020-11-05 9:12 AM rapid Exp $
 */
@RequiredArgsConstructor
public class PropertyNode {
    /**
     * the identifier of the tree
     * can be null for dynamic node
     * i.e., the id of the node is determined when run-time
     */
    public String identifier;

    /**
     * the access slot
     *
     * @see ranttu.rapid.jexp.runtime.indy.JExpIndyFactory#nextSlotNo
     */
    public int slotNo;

    /**
     * the children of this node
     */
    public Map<String, PropertyNode> children = new HashMap<>();

    /**
     * whether this node is root or not
     */
    public boolean isRoot = true;

    /**
     * the variable index for this node
     */
    public int variableIndex;

    /**
     * related closure of this property
     */
    /* package-private */ final NameClosure closure;

    /**
     * whether this node is static
     */
    public boolean isStatic() {
        return identifier != null;
    }

    /**
     * calculate the number of child need to be duplicated on stack
     */
    public int needDupChildrenCount() {
        int cnt = 0;
        for (var child : children.values()) {
            cnt += (child.isStatic() ? 1 : 0);
        }

        return cnt;
    }

    /* package-private */ void visit(Consumer<PropertyNode> tv) {
        tv.accept(this);

        for (PropertyNode child : children.values()) {
            child.visit(tv);
        }
    }
}