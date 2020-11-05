/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import ranttu.rapid.jexp.compile.closure.PropertyNode;

/**
 * a node that can fetch a property
 *
 * @author rapid
 * @version $Id: PropertyAccessNode.java, v 0.1 2018年02月24日 5:56 PM rapid Exp $
 */
public abstract class PropertyAccessNode extends ExpressionNode {
    /**
     * access node on the access tree of this ast node
     */
    public PropertyNode propertyNode;

    /**
     * if this is a static access?
     */
    public boolean isStatic = false;

    /**
     * the slot no of this node when access
     *
     * @see ranttu.rapid.jexp.runtime.indy.JExpIndyFactory#nextSlotNo
     */
    public int slotNo;

    /**
     * if has a property node, use node's
     * else, use selves
     */
    public int getSlotNo() {
        return propertyNode == null ? slotNo : propertyNode.slotNo;
    }
}