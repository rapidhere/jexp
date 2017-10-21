/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import org.apache.commons.beanutils.PropertyUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * @author rapid
 * @version $Id: TestUtil.java, v 0.1 2017年10月21日 5:23 PM rapid Exp $
 */
final public class TestUtil {
    private TestUtil() {
    }

    public static InputStream getTestResource(Class klass) {
        String className = klass.getSimpleName();

        return klass.getClassLoader().getResourceAsStream("testres/" + className + ".yaml");
    }

    public static Object fillObject(Object obj) {
        try {
            return innerFillObject(obj);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Object innerFillObject(Object obj) throws Throwable {
        if (!(obj instanceof Map)) {
            return obj;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> raw = (Map<String, Object>) obj;

        if (!raw.containsKey("class")) {
            for (String key : raw.keySet()) {
                raw.put(key, innerFillObject(raw.get(key)));
            }
            return raw;
        } else {
            Class klass = Class.forName((String) raw.get("class"));
            Object result = klass.newInstance();

            for (String key : raw.keySet()) {
                if (key.equals("class")) {
                    continue;
                }

                PropertyUtils.setProperty(result, key, innerFillObject(raw.get(key)));
            }
            return result;
        }
    }
}