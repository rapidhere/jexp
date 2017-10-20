/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

import java.util.Map;

/**
 * @author rapid
 * @version $Id: MapAccessor.java, v 0.1 2017年10月03日 10:56 PM rapid Exp $
 */
final public class MapAccessor implements Accessor {
    private String key;

    private MapAccessor() {
    }

    public static MapAccessor of(String key) {
        MapAccessor res = new MapAccessor();
        res.key = key;

        return res;
    }

    @Override
    public boolean isSatisfied(Object o) {
        return o instanceof Map;
    }

    @Override
    public Object get(Object o) {
        return ((Map) o).get(key);
    }
}