package ranttu.rapid.jexp.exception;

/**
 * duplicated name
 *
 * @author rapidhere@gmail.com
 * @version $Id: UnexpectedEOF.java, v0.1 2017-07-28 4:20 PM dongwei.dq Exp $
 */
public class DuplicatedFinalQueryClause extends JExpCompilingException {
    public DuplicatedFinalQueryClause() {
        super("linq exp can only have one select/group clause");
    }
}
