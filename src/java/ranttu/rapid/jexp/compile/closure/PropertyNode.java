/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.closure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.var;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
     * parent of this node
     */
    public PropertyNode parent;

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
     * the variable index for this node
     */
    public int variableIndex = -1;

    /**
     * the closure index of this node
     */
    public int closureIndex = -1;

    /**
     * is this a function parameter
     */
    public boolean functionParameter = false;

    /**
     * the index when this node is the parameter of a function
     */
    public int functionParameterIndex = -1;

    /**
     * this name has accessed from current closure or childrens
     */
    public boolean accessedFromLocalOrChildren = false;

    /**
     * is this a linq parameter
     */
    public boolean linqParameter = false;

    /**
     * the index when this node is the parameter of a linq expression
     */
    public int linqParameterIndex = -1;

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

    /**
     * test this node is root or not
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * visit on tree, dfs
     *
     * @param tv return true on continue visit children
     */
    /* package-private */ void visit(Function<PropertyNode, Boolean> tv) {
        var res = tv.apply(this);

        if (res) {
            for (PropertyNode child : children.values()) {
                child.visit(tv);
            }
        }
    }
}