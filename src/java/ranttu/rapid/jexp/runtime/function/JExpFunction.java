package ranttu.rapid.jexp.runtime.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark the function as a jExp function
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunction.java, v0.1 2017-08-03 1:49 PM dongwei.dq Exp $
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JExpFunction {
    /** the name (identifier) of the function */
    String name();

    /** whether this function is inlinable, default is true */
    boolean inline() default true;
}