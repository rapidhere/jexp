package ranttu.rapid.jexp.base;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * base test class
 * @author rapidhere@gmail.com
 * @version $Id: JExpTestBase.java, v0.1 2017-07-28 6:53 PM dongwei.dq Exp $
 */
abstract public class JExpTestBase extends TestNG {
    @DataProvider(name = "load-from-yaml")
    public Iterator<Object[]> loadFromYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<CaseData> data = mapper.readValue(TestUtil.getTestResource(getClass()),
            mapper.getTypeFactory().constructCollectionType(List.class, CaseData.class));
        for (CaseData caseData : data) {
            caseData.ctx = TestUtil.fillObject(caseData.ctx);
        }

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

}
