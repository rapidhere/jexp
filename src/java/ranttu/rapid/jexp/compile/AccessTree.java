/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.ast.PropertyAccessNode;

import java.util.HashMap;
import java.util.Map;

/**
 * a tree of identifier
 * @author rapid
 * @version $Id: AccessTree.java, v 0.1 2017年10月09日 8:41 AM rapid Exp $
 */
public class AccessTree {
    private AccessNode root      = new AccessNode();

    private int        slotCount = 0;

    /**
     * add to root
     */
    public void addToRoot(PropertyAccessNode astNode, String id) {
        add(root, astNode, id);
    }

    /**
     * add to parent
     */
    public void add(AccessNode parent, PropertyAccessNode astNode, String id) {
        astNode.accessNode = parent.children.computeIfAbsent(id, key -> {
            AccessNode newNode = new AccessNode();
            newNode.identifier = id;
            newNode.accessorSlot = nextSlot();
            newNode.isRoot = false;

            return newNode;
        });
    }

    /**
     * travel through the tree
     */
    public void visit(TreeVisitor tv) {
        root.visit(tv);
    }

    private String nextSlot() {
        return "accessor$" + slotCount++;
    }

    /**
     *  a access node
     */
    public static class AccessNode {
        public String                  identifier;

        public String                  accessorSlot;

        public Map<String, AccessNode> children = new HashMap<>();

        public boolean                 isRoot   = true;

        public boolean                 isAccessPoint;

        public int                     variableIndex;

        private void visit(TreeVisitor tv) {
            tv.visit(this);

            for (AccessNode child : children.values()) {
                child.visit(tv);
            }
        }
    }

    public interface TreeVisitor {
        void visit(AccessNode idNode);
    }
}