package ranttu.rapid.jexp.runtime.function;

import lombok.experimental.var;
import ranttu.rapid.jexp.exception.JExpFunctionLoadException;
import ranttu.rapid.jexp.runtime.function.builtin.CommonFunctions;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.function.builtin.StringFunctions;
import ranttu.rapid.jexp.runtime.indy.MH;
import ranttu.rapid.jexp.runtime.stream.StreamFunctions;

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
        // for debug
        // $.printClass(callClass.getSimpleName(), classBytes);

        // filter methods
        for (Method m : callClass.getMethods()) {
            if (m.isAnnotationPresent(JExpFunction.class)) {
                onStaticFunction(m);
            }

            if (m.isAnnotationPresent(JExpExtensionMethod.class)) {
                onExtensionMethod(m);
            }
        }
    }

    private static void onStaticFunction(Method m) {
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
        info.name = name;
        info.method = m;

        // put into infos
        var libMap = infos.computeIfAbsent(lib, k -> new HashMap<>());
        libMap.put(name, info);
    }

    private static void onExtensionMethod(Method m) {
        JExpExtensionMethod ann = m.getAnnotation(JExpExtensionMethod.class);
        // get name
        var name = ann.name();
        if (name.length() == 0) {
            name = m.getName();
        }

        // modifier check
        if (!Modifier.isStatic(m.getModifiers())
            || m.isVarArgs() || !Modifier.isPublic(m.getModifiers())) {
            throw new JExpFunctionLoadException(
                "java extension method can only be public static with no varargs: " + m.toString());
        }

        // this check
        var pars = m.getParameters();
        if (pars.length == 0
            || !pars[0].isAnnotationPresent(This.class)) {
            throw new JExpFunctionLoadException(
                "java extension method must have a annotation This on first parameter " + m.toString());
        }

        var extTarget = pars[0].getType();

        // dup check
        var cluster = MH.getAllMethods(extTarget);

        if (cluster.hasDeclared(name)) {
            throw new JExpFunctionLoadException(
                "target class " + extTarget.getName() + " already have a method named " + name);
        }

        // add method handle
        cluster.addDeclared(name, MH.wrapBoundedInvokeMethod(m, true));
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
}
