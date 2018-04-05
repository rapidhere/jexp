/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

/**
 * the accessor that always failed
 *
 * @author rapid
 * @version $Id: DummyAccessor.java, v 0.1 2017年10月20日 7:26 PM rapid Exp $
 */
final public class DummyAccessor implements Accessor {
    public static final DummyAccessor ACCESSOR = new DummyAccessor();

    @Override
    public boolean isSatisfied(Object o) {
        return false;
    }

    @Override
    public Object get(Object o, String key) {
        return null;
    }
}