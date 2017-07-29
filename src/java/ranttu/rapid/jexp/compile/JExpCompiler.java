package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.compile.parse.JExpParser;
import ranttu.rapid.jexp.compile.parse.Token;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.indy.JExpIndyFactory;
import ranttu.rapid.jexp.indy.JIndyType;

import java.util.HashMap;
import java.util.Map;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.INT_TYPE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the jexp compiler
 *
 * @author rapidhere@gmail.com
 * @version $Id: Compiler.java, v0.1 2017-07-27 7:58 PM dongwei.dq Exp $
 */
public class JExpCompiler implements Opcodes {
    /** the id-type binding offered by user */
    private Map<String, Class>     bindingTypes;

    /** the compile option */
    private final CompileOption    option;

    /** the jexp class loader */
    private static JExpClassLoader jExpClassLoader = new JExpClassLoader(
                                                       JExpCompiler.class.getClassLoader());

    /** the class writer */
    private ClassWriter            cw;

    /** the method visitor */
    private MethodVisitor          mv;

    /** the name count */
    private static long            nameCount       = 0;

    public JExpCompiler() {
        this.option = new CompileOption();
    }

    /**
     * compile the expression with binding types
     * @param expression        expression to compile
     * @param bindingTypes      static type binding, can be null
     * @return                  compiled expression
     * @throws JExpCompilingException   compile failed exception info
     */
    public JExpExecutable compile(String expression, Map<String, Class> bindingTypes)
                                                                                     throws JExpCompilingException {
        this.bindingTypes = bindingTypes;
        if (this.bindingTypes == null) {
            this.bindingTypes = new HashMap<>();
        }

        AstNode ast = JExpParser.parse(expression);

        // prepare
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String clsName = nextName();
        visitClass(clsName.replace('.', '/'));
        TypeUnit rType = visit(ast);

        // return
        genReturn(rType);

        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        // write class
        byte[] byteCodes = cw.toByteArray();

        // for debug
        // $.printClass(clsName, byteCodes);

        @SuppressWarnings("unchecked")
        Class<JExpExecutable> klass = jExpClassLoader.defineClass(clsName, byteCodes);

        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new JExpCompilingException("error when instance compiled class", e);
        }
    }

    private String nextName() {
        return "ranttu.rapid.jexp.JExpCompiledExpression$" + nameCount++;
    }

    private void genReturn(TypeUnit retType) {
        if (retType.isConstant) {
            mv.visitLdcInsn(retType.value);
        }

        if (retType.type == Type.INT_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
        } else if (retType.type == Type.DOUBLE_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
                "(I)Ljava/lang/Double;", false);
        }
        // otherwise, do nothing
        mv.visitInsn(ARETURN);
    }

    private void visitClass(String name) {
        if (option.tagetVersion.equals(CompileOption.JAVA_VERSION_17)) {
            cw.visit(V1_7, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, name, null,
                getInternalName(Object.class),
                new String[] { getInternalName(JExpExecutable.class) });
            cw.visitSource("<jexp-gen>", null);

            // construct method
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // `execute` method
            mv = cw.visitMethod(ACC_SYNTHETIC + ACC_PUBLIC, "execute",
                getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
            mv.visitParameter("this", 0);
            mv.visitParameter("context", 0);
            mv.visitCode();
        } else {
            throw new JExpCompilingException("unknown java version");
        }
    }

    private TypeUnit visit(AstNode astNode) {
        switch (astNode.type) {
            case PRIMARY_EXP:
                return visit((PrimaryExpression) astNode);
            case BINARY_EXP:
                return visit((BinaryExpression) astNode);
            default:
                return $.notSupport(astNode.type);
        }
    }

    private TypeUnit visit(BinaryExpression binary) {
        // support integer only
        TypeUnit leftUnit = visit(binary.left), rightUnit = visit(binary.right);

        if (leftUnit.isConstant && rightUnit.isConstant) {
            switch (binary.op.type) {
                case PLUS:
                    return constantUnit(INT_TYPE, (Integer) leftUnit.value
                                                  + (Integer) rightUnit.value);
                case SUBTRACT:
                    return constantUnit(INT_TYPE, (Integer) leftUnit.value
                                                  - (Integer) rightUnit.value);
                case MULTIPLY:
                    return constantUnit(INT_TYPE, (Integer) leftUnit.value
                                                  * (Integer) rightUnit.value);
                case DIVIDE:
                    return constantUnit(INT_TYPE, (Integer) leftUnit.value
                                                  / (Integer) rightUnit.value);
                case MODULAR:
                    return constantUnit(INT_TYPE, (Integer) leftUnit.value
                                                  % (Integer) rightUnit.value);
                default:
                    return $.notSupport(binary.op.type);
            }
        } else {
            switch (binary.op.type) {
                case PLUS:
                    mv.visitInsn(IADD);
                    return typeUnit(INT_TYPE);
                case SUBTRACT:
                    mv.visitInsn(ISUB);
                    return typeUnit(INT_TYPE);
                case MULTIPLY:
                    mv.visitInsn(IMUL);
                    return typeUnit(INT_TYPE);
                case DIVIDE:
                    mv.visitInsn(IDIV);
                    return typeUnit(INT_TYPE);
                case MODULAR:
                    mv.visitInsn(IREM);
                    return typeUnit(INT_TYPE);
                default:
                    return $.notSupport(binary.op.type);
            }
        }
    }

    private TypeUnit visit(PrimaryExpression primary) {
        Token t = primary.token;

        switch (t.type) {
            case STRING:
                return constantUnit(Type.getType(String.class), t.getString());
            case INTEGER:
                return constantUnit(Type.INT_TYPE, t.getInt());
            default:
                return $.notSupport(t.type);
        }
    }

    private TypeUnit typeUnit(Type type) {
        TypeUnit typeUnit = new TypeUnit();
        typeUnit.type = type;
        return typeUnit;
    }

    private TypeUnit constantUnit(Type type, Object constantVal) {
        TypeUnit typeUnit = new TypeUnit();
        typeUnit.type = type;
        typeUnit.isConstant = true;
        typeUnit.value = constantVal;
        return typeUnit;
    }

    private void indy(JIndyType type, String name) {
        mv.visitInvokeDynamicInsn(type.name(), "", JExpIndyFactory.INDY_CALLSITE, name);
    }
}
