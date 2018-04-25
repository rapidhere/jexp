/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import ranttu.rapid.jexp.compile.CompilingContext;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;

/**
 * @author dongwei.dq
 * @version $Id: Pass.java, v0.1 2017-08-24 6:08 PM dongwei.dq Exp $
 */
public interface Pass {
    void apply(ExpressionNode astNode, CompilingContext context);
}
