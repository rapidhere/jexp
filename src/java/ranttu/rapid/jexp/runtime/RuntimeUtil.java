/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * @author rapid
 * @version $Id: RuntimeUtil.java, v 0.1 2018年04月24日 6:16 PM rapid Exp $
 */
@SuppressWarnings("unused")
@UtilityClass
public class RuntimeUtil {
    @SneakyThrows
    public Object noSuchMethod(String methodName, String className) {
        throw new NoSuchMethodException(className + "#" + methodName);
    }
}