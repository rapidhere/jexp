/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.TypeUtil;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.CompilingContext;
import ranttu.rapid.jexp.compile.IdentifierTree;
import ranttu.rapid.jexp.compile.JExpByteCodeTransformer;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.runtime.JExpClassLoader;
import ranttu.rapid.jexp.compile.JExpExpression;
import ranttu.rapid.jexp.compile.JExpImmutableExpression;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.FunctionExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.accesor.Accessor;
import ranttu.rapid.jexp.runtime.accesor.AccessorFactory;
import ranttu.rapid.jexp.runtime.accesor.DummyAccessor;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getDescriptor;
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
            context.compiledStub = JExpImmutableExpression.of(root.constantValue);
            return;
        }

        this.context = context;
        this.context.inlinedLocalVarCount = 2;

        // prepare class
        visitClass();

        // visit the method
        visit(root);
        mathOpValConvert(root);

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
            Class<JExpExpression> klass = JExpClassLoader.define(context.className, byteCodes);
            context.compiledStub = klass.newInstance();
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

    private void visitClass() {
        // build identifier tree
        for (String id : context.identifierCountMap.keySet()) {
            context.identifierTree.add(id);
        }

        if (context.option.targetJavaVersion.equals(CompileOption.JAVA_VERSION_16)) {
            cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, context.classInternalName, null,
                getInternalName(Object.class),
                new String[] { getInternalName(JExpExpression.class) });
            cw.visitSource("<jexp-expression>", null);

            // construct method
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V", false);

            // put accessor init
            context.identifierTree.visit(this::initAccessor);

            // return
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // `execute` method
            mv = cw.visitMethod(ACC_SYNTHETIC + ACC_PUBLIC, "execute",
                getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
            mv.visitParameter("this", 0);
            mv.visitParameter("context", 0);
            mv.visitCode();

            // store all identifier values on stack
            context.identifierTree.visit(idNode -> {
                //~~~ put
                if (idNode.root) {
                    if (idNode.children.size() == 0) {
                        return;
                    }

                    mv.visitVarInsn(ALOAD, 1);
                } else {
                    invokeAccessor(idNode);
                }

                //~~~ store
                if (idNode.isLeaf()) {
                    // get store index
                    int varIndex = context.inlinedLocalVarCount;
                    context.inlinedLocalVarCount++;
                    context.identifierInlineVarMap.put(idNode.path, varIndex);

                    mv.visitVarInsn(ASTORE, varIndex);
                } else {
                    for (int i = 0; i < idNode.children.size() - 1; i++) {
                        mv.visitInsn(DUP);
                    }
                }
            });
        } else {
            throw new JExpCompilingException("unknown java version"
                                             + context.option.targetJavaVersion);
        }
    }

    private void initAccessor(IdentifierTree.IdentifierNode idInfo) {
        if (!idInfo.root) {
            // add a field to the impl
            cw.visitField(ACC_PRIVATE + ACC_SYNTHETIC, idInfo.accessorSlot,
                getDescriptor(Accessor.class), null, null);

            // add field init
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, getInternalName(DummyAccessor.class), "ACCESSOR",
                getDescriptor(DummyAccessor.class));
            mv.visitFieldInsn(PUTFIELD, context.classInternalName, idInfo.accessorSlot,
                getDescriptor(Accessor.class));
        }
    }

    private void invokeAccessor(IdentifierTree.IdentifierNode idNode) {
        // load accessor
        // current stack: top -> [ object, accessor, object ]
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, context.classInternalName, idNode.accessorSlot,
            getDescriptor(Accessor.class));
        mv.visitInsn(SWAP);
        Label successLabel = new Label();

        // call isSatisfied
        // current stack: top -> [ object ]
        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Accessor.class), "isSatisfied",
            "(Ljava/lang/Object;)Z", true);
        mv.visitJumpInsn(IFNE, successLabel);

        // failed, change accessor
        mv.visitInsn(DUP); // [ object, object ]
        mv.visitLdcInsn(idNode.identifier);
        mv.visitMethodInsn(
            INVOKESTATIC,
            getInternalName(AccessorFactory.class),
            "getAccessor",
            getMethodDescriptor(getType(Accessor.class), getType(Object.class),
                getType(String.class)), false); // [ accessor, object ]
        mv.visitVarInsn(ALOAD, 0); //  [ this, accessor, object ]
        mv.visitInsn(SWAP); //  [ accessor, this, object ]
        mv.visitFieldInsn(PUTFIELD, context.classInternalName, idNode.accessorSlot,
            getDescriptor(Accessor.class)); // [ object ]

        // then, call get method
        mv.visitLabel(successLabel);
        mv.visitFrame(F_SAME1, 0, null, 1, new Object[] { getInternalName(Object.class) });
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, context.classInternalName, idNode.accessorSlot,
            getDescriptor(Accessor.class));
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Accessor.class), "get",
            "(Ljava/lang/Object;)Ljava/lang/Object;", true);
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
            mv.visitVarInsn(ALOAD, context.identifierInlineVarMap.get(exp.getId()));
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
