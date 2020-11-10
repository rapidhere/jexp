/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.closure;

/**
 * the root name closure
 *
 * @author rapid
 * @version : IndependentNameClosure.java, v 0.1 2020-11-05 9:02 AM rapid Exp $
 */
/* package-private */ class RootNameClosure extends NameClosure {
    public RootNameClosure() {
        super(null);
    }

    /**
     * @see NameClosure#addNameAccessOnParent(String)
     */
    @Override
    public PropertyNode addNameAccessOnParent(String id) {
        throw new UnsupportedOperationException();
    }
}