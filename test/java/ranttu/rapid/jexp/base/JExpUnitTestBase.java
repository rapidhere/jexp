/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import org.testng.AssertJUnit;
import org.testng.SkipException;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.JExpExpression;

/**
 * @author rapid
 * @version $Id: JExpUnitTestBase.java, v 0.1 2017年10月01日 10:23 AM rapid Exp $
 */
@Test
public class JExpUnitTestBase extends JExpTestBase {
    @Test(dataProvider = "load-from-yaml")
    public void testExpression(CaseData caseData) {
        if (caseData.skip) {
            throw new SkipException("skipped by case data mark");
        }

        CompileOption op = new CompileOption();
        op.debugInfo = false;
        op.treatGetterNoSideEffect = true;

        JExpExpression exp = JExp.compile(caseData.exp, op);
        Object res = exp.execute(caseData.ctx);

        AssertJUnit.assertEquals(caseData.res, res);
    }
}