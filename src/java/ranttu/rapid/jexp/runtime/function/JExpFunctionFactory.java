package ranttu.rapid.jexp.runtime.function;

import lombok.experimental.var;
import org.apache.commons.io.IOUtils;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.exception.JExpFunctionLoadException;
import ranttu.rapid.jexp.runtime.function.builtin.CommonFunctions;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.function.builtin.StreamFunctions;
import ranttu.rapid.jexp.runtime.function.builtin.StringFunctions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * the function factory of jExp
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunctionFactory.java, v0.1 2017-08-03 1:58 PM dongwei.dq Exp $
 */
final public class JExpFunctionFactory {
    private JExpFunctionFactory() {
    }

    // function function lib -> name -> function info
    private final static Map<String, Map<String, FunctionInfo>> infos = new HashMap<>();

    private final static String DEFAULT_LIB_NAME = "$DEFAULT";

    // builtin register
    static {
        register(StringFunctions.class);
        register(CommonFunctions.class);
        register(JExpLang.class);
        register(StreamFunctions.class);
    }

    /**
     * register a new function class
     */
    public synchronized static void register(Class<?> callClass) throws JExpFunctionLoadException {
        // load class bytes
        var classBytes = loadClassByteCode(callClass);

        // for debug
        // $.printClass(callClass.getSimpleName(), classBytes);

        var infoCollectMap = new HashMap<String, FunctionInfo>();

        // filter methods
        for (Method m : callClass.getMethods()) {
            if (m.isAnnotationPresent(JExpFunction.class)) {
                JExpFunction ann = m.getAnnotation(JExpFunction.class);
                // get name
                var name = ann.name();
                if (name.length() == 0) {
                    name = m.getName();
                }

                // get lib
                var lib = ann.lib();
                if (lib.length() == 0) {
                    lib = DEFAULT_LIB_NAME;
                }

                // modifier check
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new JExpFunctionLoadException(
                        "java function can only be static: " + name);
                }

                // update function info
                if (infos.containsKey(lib) && infos.get(lib).containsKey(name)) {
                    throw new JExpFunctionLoadException(
                        "function name duplicated: " + name + " in lib: " + lib);
                }

                var info = new FunctionInfo();
                info.byteCodes = classBytes;
                info.name = name;
                info.inline = ann.inline();
                info.method = m;

                // put into infos
                var libMap = infos.computeIfAbsent(lib, k -> new HashMap<>());
                libMap.put(name, info);

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
        return getInfo(DEFAULT_LIB_NAME, name);
    }

    /**
     * get the info of the function by lib-name and name
     */
    public static Optional<FunctionInfo> getInfo(String libName, String name) {
        if (infos.containsKey(libName)) {
            return Optional.ofNullable(infos.get(libName).get(name));
        } else {
            return Optional.empty();
        }
    }

    // load class byte code from class
    private static byte[] loadClassByteCode(Class<?> klass) {
        // get klass file path
        var classPath = klass.getName().replace(".", "/") + ".class";

        // load class source
        var ins = JExpFunctionFactory.class.getClassLoader().getResourceAsStream(classPath);

        try {
            $.should(ins != null);
            return IOUtils.toByteArray(ins);
        } catch (IOException e) {
            throw new JExpFunctionLoadException("failed to load class file " + classPath, e);
        }
    }
}
