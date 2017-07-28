package ranttu.rapid.jexp.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.testng.AssertJUnit;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * base test class
 * @author rapidhere@gmail.com
 * @version $Id: JExpTestBase.java, v0.1 2017-07-28 6:53 PM dongwei.dq Exp $
 */
@Test
abstract public class JExpTestBase extends TestNG {
    @Test(dataProvider = "load-from-yaml")
    public void testExpression(CaseData caseData) {
        Object res = JExp.eval(caseData.exp, caseData.ctx);
        AssertJUnit.assertEquals(res, caseData.res);
    }

    @DataProvider(name = "load-from-yaml")
    public Iterator<Object[]> loadFromYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<CaseData> data = mapper.readValue(getTestResource(), mapper.getTypeFactory()
            .constructCollectionType(List.class, CaseData.class));

        Iterator<CaseData> iter = data.iterator();

        return new Iterator<Object[]>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object[] next() {
                return new Object[] { iter.next() };
            }
        };
    }

    private InputStream getTestResource() {
        String className = getClass().getSimpleName();

        return getClass().getClassLoader().getResourceAsStream("testres/" + className + ".yaml");
    }
}
