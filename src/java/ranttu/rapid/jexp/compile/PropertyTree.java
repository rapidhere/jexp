/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.compile.parse.ast.PropertyAccessNode;
import ranttu.rapid.jexp.runtime.indy.JExpIndyFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * a tree of identifier
 *
 * @author rapid
 * @version $Id: AccessTree.java, v 0.1 2017年10月09日 8:41 AM rapid Exp $
 */
@RequiredArgsConstructor
public class PropertyTree {
    private PropertyNode root = new PropertyNode();

    /**
     * add to root
     */
    public void addToRoot(PropertyAccessNode astNode, String id) {
        add(root, astNode, id);
    }

    /**
     * add to parent
     */
    public void add(PropertyNode parent, PropertyAccessNode astNode, String id) {
        astNode.propertyNode = parent.children.computeIfAbsent(id, key -> {
            PropertyNode newNode = new PropertyNode();
            newNode.identifier = id;
            newNode.isRoot = false;
            newNode.slotNo = JExpIndyFactory.nextSlotNo();

            return newNode;
        });
    }

    /**
     * travel through the tree
     */
    public void visit(TreeVisitor tv) {
        root.visit(tv);
    }

    /**
     * a access node
     */
    public static class PropertyNode {
        public String identifier;

        public int slotNo;

        public Map<String, PropertyNode> children = new HashMap<>();

        public boolean isRoot = true;

        public boolean isAccessPoint;

        public int variableIndex;

        private void visit(TreeVisitor tv) {
            tv.visit(this);

            for (PropertyNode child : children.values()) {
                child.visit(tv);
            }
        }
    }

    public interface TreeVisitor {
        void visit(PropertyNode idNode);
    }
}