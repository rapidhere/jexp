package ranttu.rapid.jexp.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * base test class
 * @author rapidhere@gmail.com
 * @version $Id: JExpTestBase.java, v0.1 2017-07-28 6:53 PM dongwei.dq Exp $
 */
abstract public class JExpTestBase extends TestNG {
    @DataProvider(name = "load-from-yaml")
    public Iterator<Object[]> loadFromYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<CaseData> data = mapper.readValue(getTestResource(), mapper.getTypeFactory()
            .constructCollectionType(List.class, CaseData.class));
        for (CaseData caseData : data) {
            caseData.ctx = fillObject(caseData.ctx);
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

    protected InputStream getTestResource() {
        String className = getClass().getSimpleName();

        return getClass().getClassLoader().getResourceAsStream("testres/" + className + ".yaml");
    }

    protected static Object fillObject(Object obj) {
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

                PropertyUtils.setProperty(result, key, raw.get(key));
            }
            return result;
        }
    }
}
