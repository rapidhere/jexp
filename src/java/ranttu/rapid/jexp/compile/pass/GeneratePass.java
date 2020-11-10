/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import lombok.SneakyThrows;
import lombok.experimental.var;
import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.ByteCodes;
import ranttu.rapid.jexp.common.Types;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.CompilingContext;
import ranttu.rapid.jexp.compile.JExpByteCodeTransformer;
import ranttu.rapid.jexp.compile.closure.NameClosure;
import ranttu.rapid.jexp.compile.closure.PropertyNode;
import ranttu.rapid.jexp.compile.constant.DebugNo;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.LambdaExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqFromClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqSelectClause;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.JExpFunctionLoadException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.JExpImmutableExpression;
import ranttu.rapid.jexp.runtime.RuntimeCompiling;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.function.builtin.StreamFunctions;
import ranttu.rapid.jexp.runtime.indy.JExpCallSiteType;
import ranttu.rapid.jexp.runtime.indy.JExpIndyFactory;
import ranttu.rapid.jexp.runtime.stream.JExpLinqStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the pass that generate byte codes
 *
 * @author dongwei.dq
 * @version $Id: GeneratePass.java, v0.1 2017-08-24 10:16 PM dongwei.dq Exp $
 */
public class GeneratePass extends NoReturnPass<GeneratePass.GenerateContext> implements Opcodes {
    /**
     * @see GenerateContext#cw
     */
    private ClassWriter cw;

    /**
     * @see GenerateContext#mv
     */
    private MethodVisitor mv;

    /**
     * @see GenerateContext#conMv
     */
    private MethodVisitor conMv;

    @Override
    public void apply(ExpressionNode root, CompilingContext compilingContext) {
        if (root.isConstant) {
            compilingContext.compiledStub = JExpImmutableExpression.of(root.constantValue);
            return;
        }

        //~~~ public init
        this.compilingContext = compilingContext;

        //~~~ generate context init
        var ctx = newCtx(GCtxType.ROOT_EXP, RuntimeCompiling.nextExpressionName(),
            compilingContext.names);
        // reserved 2
        ctx.variableCount = 2;

        // run in context
        Class<JExpExpression> klass = ctx.wrap(() -> {
            // prepare class
            visitClass();

            // prepare access tree
            if (compilingContext.option.treatGetterNoSideEffect) {
                prepareAccessTree();
            }

            // visit execute method
            appendDebugInfo(DebugNo.MAIN_CONTENT_START);
            visit(root);

            // end of execute method
            mathOpValConvert(mv, root.valueType);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // end of construct
            conMv.visitInsn(RETURN);
            conMv.visitMaxs(0, 0);
            conMv.visitEnd();

            // end of all visit
            cw.visitEnd();

            // define and return
            return defineClass();
        });

        // create expression instance
        try {
            compilingContext.compiledStub = klass.newInstance();
        } catch (Throwable e) {
            throw new JExpCompilingException("error when instance compiled class", e);
        }
    }

    /**
     * visit and put the ast on the stack
     */
    public void visitOnStack(ExpressionNode astNode) {
        visit(astNode);
    }

    private String nextConstantSlot() {
        return "constant$" + ctx().constantCount++;
    }

    private void prepareAccessTree() {
        appendDebugInfo(DebugNo.ACC_TREE_PREPARE_START);

        ctx().names.visitStaticPathOnTree(idNode -> {
            // for root node, load context on stack
            if (idNode.isRoot()) {
                // for a empty tree, do nothing
                if (!idNode.children.isEmpty()) {
                    mv.visitVarInsn(ALOAD, 1);
                }
            }
            // invoke the accessor to get the property
            else {
                invokeAccessorGetter(idNode.slotNo,
                    () -> mv.visitLdcInsn(idNode.identifier));
            }

            // dup for each child
            dupN(idNode.needDupChildrenCount() + (idNode.isRoot() ? 0 : 1) - 1);

            // if this a access point, store
            if (!idNode.isRoot()) {
                idNode.variableIndex = ctx().nextVariableIndex();
                mv.visitVarInsn(ASTORE, idNode.variableIndex);
            }
        });
    }

    private void visitClass() {
        if (compilingContext.option.targetJavaVersion.equals(CompileOption.JAVA_VERSION_17)) {
            cw.visit(V1_7, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, ctx().classInternalName, null,
                getInternalName(Object.class),
                new String[]{getInternalName(JExpExpression.class)});

            String debugSourceInfo = null;
            if (compilingContext.option.debugInfo) {
                debugSourceInfo = compilingContext.rawExpression;
            }
            cw.visitSource("<jexp-expression>", debugSourceInfo);

            // construct method
            ctx().conMv = conMv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            conMv.visitCode();
            conMv.visitVarInsn(ALOAD, 0);
            conMv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V",
                false);

            // `execute` method
            ctx().mv = mv = cw.visitMethod(ACC_SYNTHETIC + ACC_PUBLIC, "execute",
                getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
            mv.visitParameter("this", 0);
            mv.visitParameter("context", 0);
            mv.visitCode();
        } else {
            throw new JExpCompilingException(
                "unknown java version" + compilingContext.option.targetJavaVersion);
        }
    }

    private void invokeAccessorInvoker(int slotNo, CallExpression func) {
        // put method name
        mv.visitLdcInsn(func.methodName); // [ object, methodName ]

        // put arguments
        mv.visitLdcInsn(func.parameters.size());
        mv.visitTypeInsn(ANEWARRAY, getInternalName(Object.class));
        for (int i = 0; i < func.parameters.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            visit(func.parameters.get(i));
            mv.visitInsn(AASTORE);
        }

        mv.visitInvokeDynamicInsn(
            JExpCallSiteType.BD_INVOKE.name(),
            "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
            JExpIndyFactory.INDY_FACTORY_HANDLE, slotNo);
    }

    private void invokeAccessorGetter(int slotNo, Runnable invokePropertyName) {
        // [propertyOwner] -> [propertyOwner, propertyName]
        invokePropertyName.run();

        mv.visitInvokeDynamicInsn(
            JExpCallSiteType.GET_PROP.name(),
            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            JExpIndyFactory.INDY_FACTORY_HANDLE, slotNo);
    }

    @Override
    protected void visit(ExpressionNode astNode) {
        // pass through
        if (!astNode.isConstant) {
            super.visit(astNode);
            return;
        }

        // load constant
        Object val = astNode.constantValue;

        // string, directly load
        if (val instanceof String) {
            mv.visitLdcInsn(astNode.constantValue);
        }
        // integer, double, store in slot
        else if (val instanceof Integer || val instanceof Double || val instanceof Boolean) {
            String slot = ctx().constantSlots.computeIfAbsent(val, v -> {
                String newSlot = nextConstantSlot();

                // field
                cw.visitField(ACC_SYNTHETIC + ACC_PRIVATE, newSlot, getDescriptor(val.getClass()),
                    null, null);

                // field init
                conMv.visitVarInsn(ALOAD, 0);
                conMv.visitLdcInsn(val);
                mathOpValConvert(conMv, Types.getPrimitive(val.getClass()));
                conMv.visitFieldInsn(PUTFIELD, ctx().classInternalName, newSlot,
                    getDescriptor(val.getClass()));

                return newSlot;
            });

            // get field
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ctx().classInternalName, slot,
                getDescriptor(val.getClass()));
        }
        // unknown constant
        else {
            $.shouldNotReach(val + ": " + val.getClass());
        }
    }

    @Override
    protected void visit(PrimaryExpression exp) {
        // should not be a constant
        if (exp.isConstant) {
            $.shouldNotReach();
        }

        if (exp.token.is(TokenType.IDENTIFIER)) {
            $.should(exp.isStatic);
            loadIdentifierValueOnStack(exp.propertyNode);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void visit(BinaryExpression exp) {
        if (exp.op.is(TokenType.OR) || exp.op.is(TokenType.AND)) {
            onCondOp(exp);
            return;
        }

        // for float type
        if (Types.isFloat(exp.valueType)) {
            visit(exp.left);
            if (Types.isInt(exp.left.valueType)) {
                mv.visitInsn(I2D);
            }

            visit(exp.right);
            if (Types.isInt(exp.right.valueType)) {
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
        else if (Types.isInt(exp.valueType)) {
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
        // for any runtime types
        else {
            // build arguments
            List<ExpressionNode> args = new ArrayList<>();
            args.add(exp.left);
            args.add(exp.right);

            switch (exp.op.type) {
                case PLUS:
                    applyFunction("math", "add", args);
                    break;
                case SUBTRACT:
                    applyFunction("math", "sub", args);
                    break;
                case MULTIPLY:
                    applyFunction("math", "mul", args);
                    break;
                case DIVIDE:
                    applyFunction("math", "div", args);
                    break;
                case MODULAR:
                    applyFunction("math", "mod", args);
                    break;
            }
        }
    }

    private void onCondOp(BinaryExpression exp) {
        visit(exp.left);
        mv.visitInsn(DUP);
        // call Runtimes.booleanValue
        mv.visitMethodInsn(INVOKESTATIC, getInternalName(JExpLang.class), "exactBoolean",
            getMethodDescriptor(getType(boolean.class), getType(Object.class)), false);

        switch (exp.op.type) {
            case OR:
                var trueLabel = new Label();
                mv.visitJumpInsn(IFNE, trueLabel);
                mv.visitInsn(POP);
                visit(exp.right);
                mv.visitLabel(trueLabel);
                mv.visitFrame(F_SAME1, 0, null, 1, new Object[]{Types.getFrameDesc(Object.class)});
                break;
            case AND:
                var falseLabel = new Label();
                mv.visitJumpInsn(IFEQ, falseLabel);
                mv.visitInsn(POP);
                visit(exp.right);
                mv.visitLabel(falseLabel);
                mv.visitFrame(F_SAME1, 0, null, 1, new Object[]{Types.getFrameDesc(Object.class)});
                break;
        }
    }


    @Override
    protected void visit(CallExpression func) {
        // that is, a static call of inner methods
        if (!func.isBounded) {
            applyFunction(func.functionInfo, func.parameters);
        } else {
            // get caller on stack
            visit(func.caller);

            // call
            invokeAccessorInvoker(func.slotNo, func);
        }
    }

    @Override
    protected void visit(MemberExpression exp) {
        if (compilingContext.option.treatGetterNoSideEffect && exp.isStatic) {
            $.should(exp.propertyNode != null && exp.propertyNode.variableIndex >= 0);
        }

        // for static member expression
        if (exp.propertyNode != null && exp.propertyNode.variableIndex >= 0) {
            $.should(exp.isStatic);
            accessViaLocalVariable(exp.propertyNode);
        }
        // for dynamic member expression
        else {
            visit(exp.owner);
            invokeAccessorGetter(
                exp.getSlotNo(),
                () -> visit(exp.propertyName));
        }
    }

    @Override
    protected void visit(ArrayExpression exp) {
        mv.visitTypeInsn(NEW, getInternalName(ArrayList.class));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, getInternalName(ArrayList.class), "<init>", "()V", false);

        exp.items.forEach(item -> {
            mv.visitInsn(DUP);
            visit(item);
            mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(List.class), "add",
                getMethodDescriptor(getType(boolean.class), getType(Object.class)), true);
            mv.visitInsn(POP);
        });
    }

    @Override
    protected void visit(LambdaExpression exp) {
        var ctx = newCtx(GCtxType.FUNC_BODY, RuntimeCompiling.nextFunctionName(), exp.names);
        // reserved
        ctx.variableCount = 2 + exp.parameters.size();
        // init function parameter variable index
        for (String parId : exp.parameters) {
            var node = exp.names.getLocalName(parId);
            node.variableIndex = node.functionParameterIndex + 2;
        }

        Class<JExpFunctionHandle> klass = ctx.wrap(() -> {
            //~~~ prepare class
            if (!compilingContext.option.targetJavaVersion.equals(CompileOption.JAVA_VERSION_17)) {
                throw new JExpCompilingException(
                    "unknown java version" + compilingContext.option.targetJavaVersion);
            }

            cw.visit(V1_7, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, ctx().classInternalName, null,
                getInternalName(Object.class),
                new String[]{getInternalName(JExpFunctionHandle.class)});

            cw.visitSource("<jexp-lambda-function>", null);

            // construct method
            ctx().conMv = conMv =
                cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            conMv.visitCode();
            conMv.visitVarInsn(ALOAD, 0);
            conMv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V",
                false);

            // `execute` method
            ctx().mv = mv = cw.visitMethod(ACC_SYNTHETIC + ACC_PUBLIC, "invoke",
                "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            mv.visitCode();

            // prepare
            prepareFunctionExpressionMethod(ctx());

            // visit body
            appendDebugInfo(DebugNo.MAIN_CONTENT_START);
            visit(exp.body);

            // end of execute method
            if (!exp.body.isConstant) {
                mathOpValConvert(mv, exp.body.valueType);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // end of construct
            conMv.visitInsn(RETURN);
            conMv.visitMaxs(0, 0);
            conMv.visitEnd();

            // end of all visit
            cw.visitEnd();

            return defineClass();
        });

        // NOTE:
        // keep in mind, the shortcuts mv, cw, conMv is now parent context!!!

        // after function prepared, put function instance on stack
        mv.visitTypeInsn(NEW, Type.getInternalName(klass));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL
            , Type.getInternalName(klass), "<init>", "()V", false);

        // set closure arguments
        for (var idNode : ctx.names.properties.values()) {
            // filter closure node
            if (idNode.closureIndex < 0) {
                continue;
            }

            mv.visitInsn(DUP);
            // find from parent
            loadIdentifierValueOnStack(ctx.names.parent.getLocalName(idNode.identifier));
            // set field
            mv.visitFieldInsn(PUTFIELD, ctx.classInternalName, "closure$" + idNode.closureIndex,
                "Ljava/lang/Object;");
        }
    }

    @Override
    protected void visit(LinqExpression exp) {
        ctx().wrapNamesOnly(exp.names, () -> {
            exp.queryBodyClauses.forEach(this::visit);
            visit(exp.finalQueryClause);
            return null;
        });
    }

    @Override
    protected void visit(LinqFromClause exp) {
        // put name index on stack
        mv.visitLdcInsn(exp.linqParameterIndex);

        // put stream on stack
        visit(exp.sourceExp);
        ByteCodes.box(mv, exp.sourceExp.valueType);

        // TODO: move to INDY
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class), "withName",
            "(ILjava/lang/Object;)" + getDescriptor(JExpLinqStream.class),
            false);

        // if is not first from clause, call with crossJoin
        if (!exp.firstFromClause) {
            mv.visitMethodInsn(INVOKEVIRTUAL,
                getInternalName(JExpLinqStream.class),
                "crossJoin",
                getMethodDescriptor(getType(JExpLinqStream.class), getType(JExpLinqStream.class)),
                false);
        }
    }

    @Override
    protected void visit(LinqSelectClause exp) {
        // put select function
        visit(exp.lambdaExp);

        // call JExpLinqStream.select
        mv.visitMethodInsn(INVOKEVIRTUAL,
            getInternalName(JExpLinqStream.class),
            "select",
            getMethodDescriptor(getType(Stream.class), getType(JExpFunctionHandle.class))
            , false);
    }

    private void prepareFunctionExpressionMethod(GenerateContext ctx) {
        appendDebugInfo(DebugNo.ACC_TREE_PREPARE_START);

        // put arguments on stack
        ctx.names.visitStaticPathOnTree((idNode) -> {
            // omit root node
            if (idNode.isRoot()) {
                return;
            }

            // first level properties
            if (idNode.parent.isRoot()) {
                // for function parameters, store in local variables
                if (idNode.functionParameter) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(idNode.functionParameterIndex);
                    mv.visitInsn(AALOAD);
                }
                // otherwise, from closure, and store closure in local variable
                else {
                    idNode.closureIndex = ctx.nextClosureNameCount();
                    idNode.variableIndex = ctx.nextVariableIndex();
                    var closureFieldName = "closure$" + idNode.closureIndex;

                    // prepare closure field
                    cw.visitField(ACC_SYNTHETIC + ACC_PUBLIC,
                        closureFieldName, "Ljava/lang/Object;", null, null);

                    // get closure value
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD,
                        ctx.classInternalName, closureFieldName, "Ljava/lang/Object;");
                }
                // store in local
                mv.visitVarInsn(ASTORE, idNode.variableIndex);

                // when optimizing on, dup on stack
                if (compilingContext.option.treatGetterNoSideEffect) {
                    if (idNode.needDupChildrenCount() > 0) {
                        mv.visitVarInsn(ALOAD, idNode.variableIndex);
                        dupN(idNode.needDupChildrenCount() - 1);
                    }
                }
            }
            // for other nodes, visit via member-exp
            // when optimizing off, don't need to do this
            else if (compilingContext.option.treatGetterNoSideEffect) {
                // put on stack
                invokeAccessorGetter(idNode.slotNo,
                    () -> mv.visitLdcInsn(idNode.identifier));
                // dup for each child
                // one more for access point
                dupN(idNode.needDupChildrenCount());

                // store access point
                idNode.variableIndex = ctx().nextVariableIndex();
                mv.visitVarInsn(ASTORE, idNode.variableIndex);
            }
        });
    }

    private void dupN(int n) {
        for (int i = 0; i < n; i++) {
            mv.visitInsn(DUP);
        }
    }

    private void loadIdentifierValueOnStack(PropertyNode propertyNode) {
        if (compilingContext.option.treatGetterNoSideEffect) {
            $.should(propertyNode.variableIndex >= 0);
        }

        if (propertyNode.variableIndex >= 0) {
            accessViaLocalVariable(propertyNode);
        } else if (ctx().type == GCtxType.ROOT_EXP) {
            mv.visitVarInsn(ALOAD, 1);
            invokeAccessorGetter(
                propertyNode.slotNo,
                () -> mv.visitLdcInsn(propertyNode.identifier)
            );
        }
        // for function body, should all load via variable index
        else {
            $.shouldNotReach();
        }
    }

    /**
     * access a property on access tree
     */
    private void accessViaLocalVariable(PropertyNode propertyNode) {
        mv.visitVarInsn(ALOAD, propertyNode.variableIndex);
    }

    private void mathOpValConvert(MethodVisitor mv, Type valueType) {
        if (Types.isPrimitive(valueType)) {
            ByteCodes.box(mv, valueType);
        } else if (Types.isType(valueType, Object.class)) {
            mv.visitInsn(DUP);
            Label l = new Label();
            mv.visitTypeInsn(INSTANCEOF, getInternalName(StringBuilder.class));
            mv.visitJumpInsn(IFEQ, l);
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Object.class), "toString",
                getMethodDescriptor(getType(String.class)), false);
            mv.visitLabel(l);
            mv.visitFrame(F_SAME1, 0, null, 1,
                new Object[]{Types.getFrameDesc(Object.class)});
        }
    }

    // function apply util
    private void applyFunction(String lib, String functionName, List<ExpressionNode> args) {
        Optional<FunctionInfo> info = JExpFunctionFactory.getInfo(lib, functionName);
        if (!info.isPresent()) {
            throw new JExpCompilingException("function name not found: " + functionName);
        }

        applyFunction(info.get(), args);
    }

    private void applyFunction(FunctionInfo info, List<ExpressionNode> args) {
        if (info.inline && compilingContext.option.inlineFunction) {
            // inline the function
            JExpByteCodeTransformer.transform(info, this, mv, args, compilingContext);
        } else {
            if (args.size() != info.method.getParameterCount()) {
                throw new JExpFunctionLoadException(info.name + " has " + info.method.getParameterCount() +
                    " parameters, but give " + args.size());
            }

            // load stack
            for (ExpressionNode astNode : args) {
                visitOnStack(astNode);
            }

            // call
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(info.method.getDeclaringClass()),
                info.method.getName(), getMethodDescriptor(info.method), false);
        }
    }

    private void appendDebugInfo(DebugNo debugNo) {
        if (compilingContext.option.debugInfo) {
            var lb = new Label();
            mv.visitLabel(lb);
            mv.visitLineNumber(debugNo.getFlag(), lb);
        }
    }

    //~~~ generate context helper
    private <T> Class<T> defineClass() {
        return RuntimeCompiling.defineTemporaryClass(ctx().className, cw.toByteArray());
    }

    private void resetShortCuts() {
        if (ctxStack.isEmpty()) {
            return;
        }

        cw = ctx().cw;
        mv = ctx().mv;
        conMv = ctx().conMv;
    }

    private GenerateContext newCtx(GCtxType type, String className, NameClosure names) {
        var ctx = new GenerateContext();
        ctx.className = className;
        ctx.classInternalName = className.replace('.', '/');
        ctx.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ctx.names = names;
        ctx.type = type;

        return ctx;
    }

    private enum GCtxType {
        ROOT_EXP,

        FUNC_BODY,
    }

    /**
     * the generate context
     */
    class GenerateContext {
        /**
         * type of this context
         */
        public GCtxType type;

        //~~~ names
        public NameClosure names;

        //~~~ compiling class name
        /**
         * the standard class name
         */
        public String className;

        /**
         * internal class name
         */
        public String classInternalName;

        //~~~ asm
        /**
         * class writer
         */
        private ClassWriter cw;

        /**
         * execute method visitor
         */
        private MethodVisitor mv;

        /**
         * constructor method visitor
         */
        private MethodVisitor conMv;

        //~~~ compiling constants
        /**
         * constant slots count
         */
        public int constantCount = 0;

        /**
         * constant slots map
         */
        public Map<Object, String> constantSlots = new HashMap<>();

        //~~~ compiling variables
        /**
         * number of variables
         */
        public int variableCount = 0;

        /**
         * number of closure parameters
         */
        public int closureNameCount = 0;

        //~~~ helpers
        public int nextVariableIndex() {
            return variableCount++;
        }

        public int nextClosureNameCount() {
            return closureNameCount++;
        }

        /**
         * run runnable in this ctx
         */
        @SneakyThrows
        public <V> V wrap(Callable<V> callable) {
            try {
                ctxStack.push(this);
                resetShortCuts();
                return callable.call();
            } finally {
                ctxStack.pop();
                resetShortCuts();
            }
        }

        /**
         * run runnable in this ctx, with names changed only
         */
        @SneakyThrows
        public <V> V wrapNamesOnly(NameClosure names, Callable<V> callable) {
            var oldNames = this.names;
            try {
                this.names = names;
                return callable.call();
            } finally {
                this.names = oldNames;
            }
        }
    }
}
