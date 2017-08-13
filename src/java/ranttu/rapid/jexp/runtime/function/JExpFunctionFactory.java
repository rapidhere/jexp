package ranttu.rapid.jexp.runtime.function;

import org.apache.commons.io.IOUtils;
import ranttu.rapid.jexp.exception.JExpFunctionLoadException;
import ranttu.rapid.jexp.runtime.function.builtin.JExpStringFunction;

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
        register(JExpStringFunction.class);
    }

    /**
     * register a new function class
     */
    public static void register(Class<?> callClass) throws JExpFunctionLoadException {
        // load class bytes
        byte[] classBytes = loadClassByteCode(callClass);

        // filter methods
        for (Method m : callClass.getMethods()) {
            if (m.isAnnotationPresent(JExpFunction.class)) {
                JExpFunction ann = m.getAnnotation(JExpFunction.class);

                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new JExpFunctionLoadException("java function can only be static: "
                                                        + ann.name());
                }

                if (infos.containsKey(ann.name())) {
                    throw new JExpFunctionLoadException("function name duplicated: " + ann.name());
                }

                FunctionInfo info = new FunctionInfo();
                info.byteCodes = classBytes;
                info.name = ann.name();
                info.retType = m.getReturnType();
                info.javaName = m.getName();

                infos.put(ann.name(), info);
            }
        }
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
