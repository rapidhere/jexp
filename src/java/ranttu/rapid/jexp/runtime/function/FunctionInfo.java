/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function;

import java.lang.reflect.Method;

/**
 * the function info of jexp function
 *
 * @author dongwei.dq
 * @version $Id: FunctionInfo.java, v0.1 2017-08-03 3:04 PM dongwei.dq Exp $
 */
public class FunctionInfo {
    // basic function info
    public String name;

    // reflection object
    public Method method;
}
