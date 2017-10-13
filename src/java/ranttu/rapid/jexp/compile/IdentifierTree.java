/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import java.util.HashMap;
import java.util.Map;

/**
 * a tree of identifier
 * @author rapid
 * @version $Id: IdentifierTree.java, v 0.1 2017年10月09日 8:41 AM rapid Exp $
 */
public class IdentifierTree {
    private IdentifierNode root      = new IdentifierNode();

    private int            slotCount = 0;

    public void add(String identifierPath) {
        IdentifierNode current = root;
        for (String id : splitIdPath(identifierPath)) {
            IdentifierNode parent = current;
            current = current.children.computeIfAbsent(id, key -> {
                IdentifierNode newNode = new IdentifierNode();
                newNode.identifier = id;
                newNode.accessorSlot = nextSlot();
                newNode.root = false;

                if (parent == root) {
                    newNode.path = id;
                } else {
                    newNode.path = parent.path + "." + id;
                }

                return newNode;
            });
        }
    }

    public void visit(TreeVisitor tv) {
        root.visit(tv);
    }

    private String nextSlot() {
        return "accessor$" + slotCount++;
    }

    public static String[] splitIdPath(String idPath) {
        return idPath.split("\\.");
    }

    public static class IdentifierNode {
        public String                      identifier;

        public String                      accessorSlot;

        public Map<String, IdentifierNode> children = new HashMap<>();

        public boolean                     root     = true;

        public String                      path;

        private void visit(TreeVisitor tv) {
            tv.visit(this);

            for (IdentifierNode child : children.values()) {
                child.visit(tv);
            }
        }

        public boolean isLeaf() {
            return children.size() == 0;
        }
    }

    public interface TreeVisitor {
        void visit(IdentifierNode idNode);
    }
}