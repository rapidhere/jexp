/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.closure;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.exception.DuplicatedName;
import ranttu.rapid.jexp.runtime.indy.JExpIndyFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * the name closure is a package contains all names under a scope
 *
 * @author rapid
 * @version : NameClosure.java, v 0.1 2020-11-05 9:02 AM rapid Exp $
 */
abstract public class NameClosure {
    /**
     * the parent closure
     */
    final public NameClosure parent;

    /**
     * the property tree root under this closure
     * a property tree is a tree of identifier
     * for example, we have such id lists:
     * - a.b.c.d.e
     * - a.b.f
     * - a.g
     * <p>
     * then we have the identifier tree:
     * a - b - c - d - e
     * |   |
     * |   -- f
     * ---- g
     */
    final protected PropertyNode rootNode = new PropertyNode(this);

    /**
     * root properties under this closure
     */
    final public Map<String, PropertyNode> properties = new HashMap<>();

    //~~~ constructors
    protected NameClosure(NameClosure parent) {
        this.parent = parent;
    }

    public static NameClosure independent(NameClosure parent) {
        return new IndependentNameClosure(parent);
    }

    public static NameClosure embedded(NameClosure parent) {
        return new EmbeddedNameClosure(parent);
    }

    public static NameClosure root() {
        return new RootNameClosure();
    }

    /**
     * get name only in this scope
     */
    public PropertyNode getLocalName(String id) {
        return properties.get(id);
    }

    /**
     * get names only in this scope
     */
    public Collection<PropertyNode> getLocalNames() {
        return properties.values();
    }

    /**
     * declare a name under this scope
     */
    public PropertyNode declareName(String id) {
        if (properties.containsKey(id)) {
            throw new DuplicatedName(id);
        }

        var node = newNode(id, rootNode);
        node.functionParameter = true;
        properties.put(id, node);

        return node;
    }

    /**
     * access a name under this closure's parent
     */
    abstract public PropertyNode addNameAccessOnParent(String id);

    /**
     * add a name access to a owner
     */
    public PropertyNode addNameAccess(PropertyNode owner, String id) {
        return owner.children.computeIfAbsent(id, k -> newNode(id, owner));
    }

    /**
     * access a name under this closure
     */
    public PropertyNode addNameAccess(String id) {
        // if can find name in current closure
        // then access the name under this closure
        if (properties.containsKey(id)) {
            var node = properties.get(id);
            var prevNode = rootNode.children.put(id, node);
            $.should(prevNode == null || prevNode == node);
            return node;
        } else {
            // for root closure, declare the name, add access to propertyTree
            if (parent == null) {
                var node = addNameAccess(rootNode, id);
                properties.put(id, node);
                return node;
            }
            // otherwise, name is access via parent closure
            else {
                return addNameAccessOnParent(id);
            }
        }
    }

    /**
     * travel through the static path on property tree, DFS
     */
    public void visitStaticPathOnTree(Consumer<PropertyNode> v) {
        rootNode.visit(propertyNode -> {
            if (propertyNode.isRoot() || propertyNode.isStatic()) {
                v.accept(propertyNode);
                return true;
            } else {
                return false;
            }
        });
    }

    //~~~ impl
    private PropertyNode newNode(String id, PropertyNode parent) {
        var newNode = new PropertyNode(this);
        newNode.identifier = id;
        newNode.parent = parent;
        newNode.slotNo = JExpIndyFactory.nextSlotNo();

        return newNode;
    }
}