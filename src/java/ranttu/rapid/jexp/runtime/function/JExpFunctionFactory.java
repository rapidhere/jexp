package ranttu.rapid.jexp.runtime.function;

import org.apache.commons.io.IOUtils;
import ranttu.rapid.jexp.exception.JExpFunctionLoadException;
import ranttu.rapid.jexp.runtime.function.builtin.CommonFunctions;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.function.builtin.StringFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * the function factory of jExp
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunctionFactory.java, v0.1 2017-08-03 1:58 PM dongwei.dq Exp $
 */
final public class JExpFunctionFactory {
    private JExpFunctionFactory() {
    }

    // function function name -> function info
    private static Map<String, FunctionInfo> infos = new HashMap<>();

    // builtin register
    static {
        register(StringFunctions.class);
        register(CommonFunctions.class);
        register(JExpLang.class);
    }

    /**
     * register a new function class
     */
    public static void register(Class<?> callClass) throws JExpFunctionLoadException {
        // load class bytes
        byte[] classBytes = loadClassByteCode(callClass);

        // for debug
        // $.printClass(callClass.getSimpleName(), classBytes);

        Map<String, FunctionInfo> infoCollectMap = new HashMap<>();

        // filter methods
        for (Method m : callClass.getMethods()) {
            if (m.isAnnotationPresent(JExpFunction.class)) {
                JExpFunction ann = m.getAnnotation(JExpFunction.class);
                // get name
                String name = ann.name();
                if (name.length() == 0) {
                    name = m.getName();
                }

                // modifier check
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new JExpFunctionLoadException("java function can only be static: " + name);
                }

                // update function info
                if (infos.containsKey(name)) {
                    throw new JExpFunctionLoadException("function name duplicated: " + name);
                }

                FunctionInfo info = new FunctionInfo();
                info.byteCodes = classBytes;
                info.name = name;
                info.inline = ann.inline();
                info.method = m;

                infos.put(name, info);

                // for inline functions, we need to collect the compiling info
                if (info.inline) {
                    infoCollectMap.put(m.getName(), info);
                }
            }
        }

        // collect info
        FunctionInfoCollector.collectInfo(classBytes, infoCollectMap);
    }

    /**
     * get the info of the function by name
     */
    public static Optional<FunctionInfo> getInfo(String name) {
        return Optional.ofNullable(infos.get(name));
    }

    // load class byte code from class
    private static byte[] loadClassByteCode(Class klass) {
        // get klass file path
        String classPath = klass.getName().replace(".", "/") + ".class";

        // load class source
        InputStream ins = JExpFunctionFactory.class.getClassLoader().getResourceAsStream(classPath);

        try {
            return IOUtils.toByteArray(ins);
        } catch (IOException e) {
            throw new JExpFunctionLoadException("failed to load class file " + classPath, e);
        }
    }
}
