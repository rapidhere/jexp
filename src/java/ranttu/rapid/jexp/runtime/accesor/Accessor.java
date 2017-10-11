/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

/**
 * property accessor
 * @author rapid
 * @version $Id: Accessor.java, v 0.1 2017年10月03日 4:36 PM rapid Exp $
 */
public interface Accessor {
    @SuppressWarnings("unused")
    boolean isSatisfied(Object o);

    @SuppressWarnings("unused")
    Object get(Object o, String key);
}