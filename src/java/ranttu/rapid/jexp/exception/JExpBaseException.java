package ranttu.rapid.jexp.exception;

/**
 * the very base exception
 * @author rapidhere@gmail.com
 * @version $Id: JExpBaseException.java, v0.1 2017-07-28 4:17 PM dongwei.dq Exp $
 */
public class JExpBaseException extends RuntimeException {
    public JExpBaseException(String message) {
        super(message);
    }

    public JExpBaseException(String message, Throwable e) {
        super(message, e);
    }
}
