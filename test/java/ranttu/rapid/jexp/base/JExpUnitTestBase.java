/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;

/**
 * @author rapid
 * @version $Id: JExpUnitTestBase.java, v 0.1 2017年10月01日 10:23 AM rapid Exp $
 */
@Test
public class JExpUnitTestBase extends JExpTestBase {
    @Test(dataProvider = "load-from-yaml")
    public void testExpression(CaseData caseData) {
        Object res = JExp.eval(caseData.exp, caseData.ctx);
        AssertJUnit.assertEquals(caseData.res, res);
    }
}