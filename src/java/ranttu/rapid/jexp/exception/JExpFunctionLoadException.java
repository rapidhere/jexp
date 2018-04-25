package ranttu.rapid.jexp.exception;

/**
 * load jExp function failed
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunctionLoadException.java, v0.1 2017-08-03 2:13 PM dongwei.dq Exp $
 */
public class JExpFunctionLoadException extends JExpBaseException {
    public JExpFunctionLoadException(String message) {
        super(message);
    }

    public JExpFunctionLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
