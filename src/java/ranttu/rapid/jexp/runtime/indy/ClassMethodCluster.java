/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.experimental.var;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rapid
 * @version : ClassMethodCluster.java, v 0.1 2020-11-11 5:00 AM rapid Exp $
 */
public class ClassMethodCluster {
    List<ClassMethodCluster> parents = new ArrayList<>();

    Map<String, MethodHandle> mhMap = new HashMap<>();

    public void addDeclared(String name, MethodHandle mh) {
        mhMap.put(name, mh);
    }

    public boolean hasDeclared(String name) {
        return mhMap.containsKey(name);
    }

    void asStaticInner(Map<String, MethodHandle> mhs) {
        mhMap.forEach(mhs::putIfAbsent);

        for (var cluster : parents) {
            cluster.asStaticInner(mhs);
        }
    }

    Map<String, MethodHandle> asStatic() {
        var res = new HashMap<String, MethodHandle>();
        asStaticInner(res);

        return res;
    }
}