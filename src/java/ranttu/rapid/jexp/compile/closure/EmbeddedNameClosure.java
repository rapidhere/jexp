/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.closure;

import lombok.experimental.var;

/**
 * the name closure embedded int parnet name closure
 *
 * @author rapid
 * @version : IndependentNameClosure.java, v 0.1 2020-11-05 9:02 AM rapid Exp $
 */
/* package-private */ class EmbeddedNameClosure extends NameClosure {
    public EmbeddedNameClosure(NameClosure parent) {
        super(parent);
    }

    /**
     * @see NameClosure#addNameAccessOnParent(String)
     */
    @Override
    public PropertyNode addNameAccessOnParent(String id) {
        // declare a name access on parent path
        var node = parent.addNameAccess(id);

        // share with parent
        properties.put(id, node);
        return node;
    }
}