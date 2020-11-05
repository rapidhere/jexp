package ranttu.rapid.jexp.exception;

/**
 * duplicated name
 *
 * @author rapidhere@gmail.com
 * @version $Id: UnexpectedEOF.java, v0.1 2017-07-28 4:20 PM dongwei.dq Exp $
 */
public class DuplicatedName extends JExpCompilingException {
    public DuplicatedName(String name) {
        super("duplicated name: " + name);
    }
}
