/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.TypeUtil;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.CompilingContext;
import ranttu.rapid.jexp.compile.JExpByteCodeTransformer;
import ranttu.rapid.jexp.compile.JExpClassLoader;
import ranttu.rapid.jexp.compile.JExpExecutable;
import ranttu.rapid.jexp.compile.JExpMutableExpression;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.FunctionExpression;
import ranttu.rapid.jexp.compile.parse.ast.LoadContextExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the pass that generate byte codes
 * @author dongwei.dq
 * @version $Id: GeneratePass.java, v0.1 2017-08-24 10:16 PM dongwei.dq Exp $
 */
public class GeneratePass extends NoReturnPass implements Opcodes {
    private ClassWriter   cw;

    private MethodVisitor mv;

    public GeneratePass() {
        this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    }

    @Override
    public void apply(AstNode root, CompilingContext context) {
        if (root.isConstant) {
            context.compiledStub = JExpMutableExpression.of(root.constantValue);
            return;
        }

        this.context = context;
        this.context.inlinedLocalVarCount = 2;

        // prepare class
        visitClass(context.className.replace('.', '/'));

        // prepare identifier values
        prepareIdentifiers();

        if (root.isConstant) {
            mv.visitLdcInsn(root.constantValue);
        } else {
            // visit the method
            visit(root);
            mathOpValConvert(root);
        }

        // return
        mv.visitInsn(ARETURN);

        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        // generate
        // write class
        byte[] byteCodes = cw.toByteArray();

        // for debug
        $.printClass(context.className, byteCodes);

        try {
            context.compiledStub = JExpClassLoader.define(context.className, byteCodes)
                .newInstance();
        } catch (Exception e) {
            throw new JExpCompilingException("error when instance compiled class", e);
        }
    }

    /**
     * visit and put the ast on the stack
     */
    public void visitOnStack(AstNode astNode) {
        visit(astNode);
    }

    private void prepareIdentifiers() {
        // store all identifier values that is greater than 1
        for (String id : context.identifierCountMap.keySet()) {
            if (context.identifierCountMap.get(id) <= 1) {
                continue;
            }

            // put prop value on stack
            putIdentifierValue(id);

            // get store index
            int varIndex = context.inlinedLocalVarCount;
            context.inlinedLocalVarCount++;
            context.identifierInlineVarMap.put(id, varIndex);

            // store
            mv.visitVarInsn(ASTORE, varIndex);
        }
    }

    private void visitClass(String name) {
        if (context.option.targetJavaVersion.equals(CompileOption.JAVA_VERSION_16)) {
            cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, name, null,
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
            throw new JExpCompilingException("unknown java version"
                                             + context.option.targetJavaVersion);
        }
    }

    @Override
    protected void visit(AstNode astNode) {
        // constant short cut
        if (astNode.isConstant) {
            mv.visitLdcInsn(astNode.constantValue);
            return;
        }
        // pass through
        super.visit(astNode);
    }

    @Override
    protected void visit(PrimaryExpression exp) {
        // should not be a constant
        if (exp.isConstant) {
            $.shouldNotReach();
        }

        if (exp.token.is(TokenType.IDENTIFIER)) {
            if (context.identifierCountMap.get(exp.getId()) > 1) {
                mv.visitVarInsn(ALOAD, context.identifierInlineVarMap.get(exp.getId()));
            } else {
                putIdentifierValue(exp.getId());
            }
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void visit(BinaryExpression exp) {
        // for Object(runtime)
        if (exp.valueType.getClassName().equals(Object.class.getName())) {
            // build arguments
            List<AstNode> args = new ArrayList<>();
            args.add(exp.left);
            args.add(exp.right);

            switch (exp.op.type) {
                case PLUS:
                    applyFunction("math.add", args);
                    break;
                case SUBTRACT:
                    applyFunction("math.sub", args);
                    break;
                case MULTIPLY:
                    applyFunction("math.mul", args);
                    break;
                case DIVIDE:
                    applyFunction("math.div", args);
                    break;
                case MODULAR:
                    applyFunction("math.mod", args);
                    break;
            }
        }
        // for float type
        else if (TypeUtil.isFloat(exp.valueType)) {
            visit(exp.left);
            if (TypeUtil.isInt(exp.left.valueType)) {
                mv.visitInsn(I2D);
            }

            visit(exp.right);
            if (TypeUtil.isInt(exp.right.valueType)) {
                mv.visitInsn(I2D);
            }

            switch (exp.op.type) {
                case PLUS:
                    mv.visitInsn(DADD);
                    break;
                case SUBTRACT:
                    mv.visitInsn(DSUB);
                    break;
                case MULTIPLY:
                    mv.visitInsn(DMUL);
                    break;
                case DIVIDE:
                    mv.visitInsn(DDIV);
                    break;
                case MODULAR:
                    mv.visitInsn(DREM);
                    break;
            }
        }
        // for int type
        else if (TypeUtil.isInt(exp.valueType)) {
            visit(exp.left);
            visit(exp.right);

            switch (exp.op.type) {
                case PLUS:
                    mv.visitInsn(IADD);
                    break;
                case SUBTRACT:
                    mv.visitInsn(ISUB);
                    break;
                case MULTIPLY:
                    mv.visitInsn(IMUL);
                    break;
                case DIVIDE:
                    mv.visitInsn(IDIV);
                    break;
                case MODULAR:
                    mv.visitInsn(IREM);
                    break;
            }
        }
    }

    @Override
    protected void visit(FunctionExpression func) {
        applyFunction(func.functionInfo, func.parameters);
    }

    @Override
    protected void visit(LoadContextExpression exp) {
        mv.visitVarInsn(ALOAD, 1);
    }

    private void putIdentifierValue(String name) {
        // load it on stack
        AstNode arg = new LoadContextExpression();
        for (String id : name.split("\\.")) {
            List<AstNode> args = new ArrayList<>();
            args.add(arg);
            args.add(PrimaryExpression.ofString(id));
            arg = new FunctionExpression("lang.get_prop", args);

            //noinspection ConstantConditions
            ((FunctionExpression) arg).functionInfo = JExpFunctionFactory.getInfo("lang.get_prop")
                .get();
        }

        visit(arg);
    }

    private void mathOpValConvert(AstNode exp) {
        if (TypeUtil.isInt(exp.valueType)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
        } else if (TypeUtil.isFloat(exp.valueType)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
                "(D)Ljava/lang/Double;", false);
        } else if (exp.valueType == Type.BOOLEAN_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                "(Z)Ljava/lang/Boolean;", false);
        }
    }

    // function apply util
    private void applyFunction(String functionName, List<AstNode> args) {
        Optional<FunctionInfo> info = JExpFunctionFactory.getInfo(functionName);
        if (!info.isPresent()) {
            throw new JExpCompilingException("function name not found: " + functionName);
        }

        applyFunction(info.get(), args);
    }

    private void applyFunction(FunctionInfo info, List<AstNode> args) {
        if (info.inline && context.option.inlineFunction) {
            // inline the function
            JExpByteCodeTransformer.transform(info, this, mv, args, context);
        } else {
            // load stack
            for (AstNode astNode : args) {
                visitOnStack(astNode);
            }

            // call
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(info.method.getDeclaringClass()),
                info.javaName, Type.getMethodDescriptor(info.method), false);
        }
    }
}
