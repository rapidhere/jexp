/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * global options from sys properties
 *
 * @author rapid
 * @version : JExpGlobalOptions.java, v 0.1 2020-11-18 2:44 PM rapid Exp $
 */
final public class JExpGlobalOptions {
    private JExpGlobalOptions() {
        throw new UnsupportedOperationException();
    }

    @Option("jexp.printBC")
    @Getter
    private static boolean printByteCodesToStdout = false;


    //~~~ impl
    static {
        loadOptions();
    }

    private static void loadOptions() {
        for (Field f : JExpGlobalOptions.class.getDeclaredFields()) {
            Option op = f.getAnnotation(Option.class);
            if (op == null) {
                continue;
            }

            String propName = op.value();
            String v = System.getProperty(propName);
            if (v != null) {
                if (f.getType() == boolean.class) {
                    setField(f, null, Boolean.parseBoolean(v));
                } else {
                    $.shouldNotReach();
                }
            }
        }
    }

    @SneakyThrows
    private static void setField(Field f, Object owner, Object val) {
        f.setAccessible(true);
        f.set(owner, val);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Option {
        String value();
    }
}