package ranttu.rapid.jexp.common;

import ranttu.rapid.jexp.exception.FunctionOpcodeNotSupportedYet;
import ranttu.rapid.jexp.exception.UnsupportedYet;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassReader;
import ranttu.rapid.jexp.external.org.objectweb.asm.util.TraceClassVisitor;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import java.io.PrintWriter;

/**
 * common utilities
 *
 * @author rapidhere@gmail.com
 * @version $Id: $.java, v0.1 2017-07-27 7:56 PM dongwei.dq Exp $
 */
final public class $ {
    private $() {
    }

    /**
     * print the class from byte code
     * @param className  the name of the class
     * @param bytes      the bytes
     */
    public static void printClass(String className, byte[] bytes) {
        if (Boolean.valueOf(System.getProperty("jexp.printBC"))) {
            System.out.println("========Class: " + className);
            ClassReader reader = new ClassReader(bytes);
            reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
            System.out.println();
        }
    }

    // ~~~ common helpers
    @SafeVarargs
    public static <T> boolean in(T o, T... toCheck) {
        for (T c : toCheck) {
            if (o.equals(c)) {
                return true;
            }
        }

        return false;
    }

    @SafeVarargs
    public static <T> boolean notIn(T o, T... toCheck) {
        return !in(o, toCheck);
    }

    public static <T> T notSupport(Object s) {
        throw new UnsupportedYet(s.toString());
    }

    public static <T> T opNotSupport(FunctionInfo info, int op) {
        throw new FunctionOpcodeNotSupportedYet(info.name, op);
    }

    public static <T> T shouldNotReach() {
        throw new AssertionError();
    }
}
