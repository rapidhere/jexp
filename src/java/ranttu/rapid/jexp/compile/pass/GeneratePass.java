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
import ranttu.rapid.jexp.compile.closure.NameClosure;
import ranttu.rapid.jexp.compile.closure.PropertyNode;
import ranttu.rapid.jexp.compile.constant.DebugNo;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ValueType;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.AstType;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.DictExpression;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.LambdaExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqFinalQueryClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqFromClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqGroupByClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqJoinClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqLetClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqOrderByClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqSelectClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqWhereClause;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.UnaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.JExpFunctionArgumentConvertException;
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
import ranttu.rapid.jexp.runtime.indy.JExpCallSiteType;
import ranttu.rapid.jexp.runtime.indy.JExpIndyFactory;
import ranttu.rapid.jexp.runtime.stream.Grouping;
import ranttu.rapid.jexp.runtime.stream.StreamFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
            mathOpValConvert(mv, root);
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
                putActualIdOnStack(mv, idNode);
                invokeAccessorGetter(idNode.slotNo);
            }

            // dup for each child
            ByteCodes.dupN(mv,
                idNode.needDupChildrenCount() + (idNode.isRoot() ? 0 : 1) - 1);

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
            var par = func.parameters.get(i);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            visit(func.parameters.get(i));
            mathOpValConvert(mv, par);
            mv.visitInsn(AASTORE);
        }

        mv.visitInvokeDynamicInsn(
            JExpCallSiteType.BD_INVOKE.name(),
            "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
            JExpIndyFactory.INDY_FACTORY_HANDLE, slotNo);
    }

    private void invokeAccessorGetter(int slotNo) {
        // [propertyOwner, propertyName]
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
        var val = astNode.constantValue;

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
                ByteCodes.box(conMv, Types.getPrimitive(val.getClass()));
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

        // build arguments
        List<ExpressionNode> args = new ArrayList<>();
        args.add(exp.left);
        args.add(exp.right);

        checkNotPrimitive(exp.left.valueType, exp.op.type.name());
        checkNotPrimitive(exp.right.valueType, exp.op.type.name());
        switch (exp.op.type) {
            case PLUS:
                applyFunction("math", "add", args, true);
                break;
            case SUBTRACT:
                applyFunction("math", "sub", args, true);
                break;
            case MULTIPLY:
                applyFunction("math", "mul", args, true);
                break;
            case DIVIDE:
                applyFunction("math", "div", args, true);
                break;
            case MODULAR:
                applyFunction("math", "mod", args, true);
                break;
            case EQEQ:
                applyFunction("lang", "eq", args, false);
                break;
            case NOT_EQ:
                applyFunction("lang", "neq", args, false);
                break;
            case GREATER:
                applyFunction("math", "grt", args, false);
                break;
            case GREATER_EQ:
                applyFunction("math", "gre", args, false);
                break;
            case SMALLER:
                applyFunction("math", "lst", args, false);
                break;
            case SMALLER_EQ:
                applyFunction("math", "lse", args, false);
                break;
        }
    }

    @Override
    protected void visit(UnaryExpression exp) {
        List<ExpressionNode> args = new ArrayList<>();
        args.add(exp.exp);

        switch (exp.op.type) {
            case SUBTRACT:
                applyFunction("math", "minus", args, true);
                break;
            case NOT:
                if (exp.exp.valueType == ValueType.BOOL
                    || exp.exp.valueType == ValueType.BOOL_WRAPPED) {
                    applyFunction("math", "bnot", args, false);
                } else {
                    applyFunction("math", "not", args, false);
                }
                break;
        }
    }

    private void checkNotPrimitive(ValueType vt, String msg) {
        if (Types.isPrimitive(vt.getType())) {
            throw new JExpCompilingException(msg + ", cannot apply type " + vt);
        }
    }

    private void onCondOp(BinaryExpression exp) {
        visit(exp.left);

        if (exp.left.valueType == ValueType.BOOL) {
            mv.visitInsn(DUP);
            ByteCodes.box(mv, Type.BOOLEAN_TYPE);
            mv.visitInsn(SWAP);
        } else {
            // call Runtimes.booleanValue
            // ByteCodes.box(mv, exp.left.valueType);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESTATIC, getInternalName(JExpLang.class), "exactBoolean",
                getMethodDescriptor(getType(boolean.class), getType(Object.class)), false);
        }

        switch (exp.op.type) {
            case OR:
                var trueLabel = new Label();
                mv.visitJumpInsn(IFNE, trueLabel);
                mv.visitInsn(POP);
                visit(exp.right);
                ByteCodes.box(mv, exp.right.valueType.getType());
                mv.visitLabel(trueLabel);
                break;
            case AND:
                var falseLabel = new Label();
                mv.visitJumpInsn(IFEQ, falseLabel);
                mv.visitInsn(POP);
                visit(exp.right);
                ByteCodes.box(mv, exp.right.valueType.getType());
                mv.visitLabel(falseLabel);
                break;
        }
    }


    @Override
    protected void visit(CallExpression func) {
        // that is, a static call of inner methods
        if (!func.isBounded) {
            applyFunction(func.functionInfo, func.parameters, true);
        } else {
            // get caller on stack
            visit(func.caller);
            mathOpValConvert(mv, func.caller);

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
            mathOpValConvert(mv, exp.owner);

            visit(exp.propertyName);
            invokeAccessorGetter(exp.getSlotNo());
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
            mathOpValConvert(mv, item);

            mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(List.class), "add",
                getMethodDescriptor(getType(boolean.class), getType(Object.class)), true);
            mv.visitInsn(POP);
        });
    }


    @Override
    protected void visit(DictExpression exp) {
        mv.visitTypeInsn(NEW, getInternalName(LinkedHashMap.class));
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, getInternalName(LinkedHashMap.class),
            "<init>", "()V", false);

        exp.items.forEach((id, itemExp) -> {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(id);
            visit(itemExp);
            mathOpValConvert(mv, itemExp);
            mv.visitMethodInsn(INVOKEINTERFACE,
                getInternalName(Map.class),
                "put",
                getMethodDescriptor(getType(Object.class),
                    getType(Object.class), getType(Object.class)),
                true);
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
            mathOpValConvert(mv, exp.body);
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

        processLinqFinalCont(exp.finalQueryClause);
    }

    private void processLinqFinalCont(LinqFinalQueryClause exp) {
        if (!exp.hasQueryContinuation()) {
            return;
        }
        // wrap to linq stream
        mv.visitLdcInsn(exp.contItemLinqParameterIndex);
        mv.visitInsn(SWAP);

        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "withName", "(ILjava/lang/Object;)" + getDescriptor(Stream.class),
            false);

        ctx().wrapNamesOnly(exp.names, () -> {
            exp.queryBodyClauses.forEach(this::visit);
            visit(exp.finalQueryClause);
            return null;
        });
    }

    @Override
    protected void visit(LinqFromClause exp) {
        // for first from clause, just load from parent, no need to join
        if (exp.firstFromClause) {
            // put name index on stack
            mv.visitLdcInsn(exp.linqParameterIndex);

            // put stream on stack
            visit(exp.sourceExp);
            mathOpValConvert(mv, exp.sourceExp);

            // TODO: move to INDY
            mv.visitMethodInsn(INVOKESTATIC,
                getInternalName(StreamFunctions.class), "withName",
                "(ILjava/lang/Object;)" + getDescriptor(Stream.class),
                false);
        }
        // if is not first from clause, call with crossJoin
        else {
            // name index on stack
            mv.visitLdcInsn(exp.linqParameterIndex);

            // put source function on stack
            visit(exp.sourceLambda);

            // for static source, call with buffered method
            mv.visitMethodInsn(INVOKESTATIC,
                getInternalName(StreamFunctions.class),
                exp.isSourceStatic ? "crossJoinStatic" : "crossJoinDynamic",
                getMethodDescriptor(getType(Stream.class),
                    getType(Stream.class), getType(int.class), getType(JExpFunctionHandle.class)),
                false);
        }
    }

    @Override
    protected void visit(LinqLetClause exp) {
        mv.visitLdcInsn(exp.linqParameterIndex);
        visit(exp.lambdaExp);

        // call JExpLinqStream.let
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "let",
            getMethodDescriptor(getType(Stream.class),
                getType(Stream.class), getType(int.class), getType(JExpFunctionHandle.class))
            , false);
    }

    @Override
    protected void visit(LinqSelectClause exp) {
        // put select function
        visit(exp.lambdaExp);

        // call JExpLinqStream.select
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "select",
            getMethodDescriptor(getType(Stream.class),
                getType(Stream.class), getType(JExpFunctionHandle.class))
            , false);
    }

    @Override
    protected void visit(LinqWhereClause exp) {
        // put where predicate
        visit(exp.lambdaExp);

        // call JExpLinqStream.where
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "where",
            getMethodDescriptor(getType(Stream.class),
                getType(Stream.class), getType(JExpFunctionHandle.class))
            , false);
    }

    @Override
    protected void visit(LinqOrderByClause exp) {
        // load lambdas
        mv.visitLdcInsn(exp.items.size());
        mv.visitTypeInsn(ANEWARRAY, getInternalName(JExpFunctionHandle.class));
        for (int i = 0; i < exp.items.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            visit(exp.items.get(i).keySelectLambda);
            mv.visitInsn(AASTORE);
        }

        // load descending flags
        mv.visitLdcInsn(exp.items.size());
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        for (int i = 0; i < exp.items.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitLdcInsn(exp.items.get(i).descending);
            mv.visitInsn(BASTORE);
        }

        // JExpLinqStream.orderBy
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "orderBy",
            getMethodDescriptor(getType(Stream.class),
                getType(Stream.class), getType(JExpFunctionHandle[].class), getType(boolean[].class))
            , false);
    }

    @Override
    protected void visit(LinqJoinClause exp) {
        // load source
        // put name index on stack
        mv.visitLdcInsn(exp.innerItemLinqParameterIndex);

        // put stream on stack
        visit(exp.sourceExp);
        mathOpValConvert(mv, exp);

        // TODO: move to INDY
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class), "withName",
            "(ILjava/lang/Object;)" + getDescriptor(Stream.class),
            false);

        // outerKey/innerKey
        visit(exp.outerKeyLambda);
        visit(exp.innerKeyLambda);

        // inner join
        if (!exp.isGroupJoin()) {
            // JExpLinqStream.join
            mv.visitMethodInsn(INVOKESTATIC,
                getInternalName(StreamFunctions.class),
                "join",
                getMethodDescriptor(getType(Stream.class),
                    getType(Stream.class), getType(Stream.class),
                    getType(JExpFunctionHandle.class), getType(JExpFunctionHandle.class))
                , false);
        }
        // group join
        else {
            mv.visitLdcInsn(exp.groupJoinItemLinqParameterIndex);
            mv.visitLdcInsn(exp.innerItemLinqParameterIndex);

            // JExpLinqStream.groupJoin
            mv.visitMethodInsn(INVOKESTATIC,
                getInternalName(StreamFunctions.class),
                "groupJoin",
                getMethodDescriptor(getType(Stream.class),
                    getType(Stream.class),
                    getType(Stream.class),
                    getType(JExpFunctionHandle.class),
                    getType(JExpFunctionHandle.class),
                    getType(int.class), getType(int.class))
                , false);
        }
    }

    @Override
    protected void visit(LinqGroupByClause exp) {
        // select handle
        visit(exp.selectLambda);

        // key handle
        visit(exp.keyLambda);

        // JExpLinqStream.group
        mv.visitMethodInsn(INVOKESTATIC,
            getInternalName(StreamFunctions.class),
            "group",
            getMethodDescriptor(getType(Grouping.class),
                getType(Stream.class),
                getType(JExpFunctionHandle.class),
                getType(JExpFunctionHandle.class))
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
                        ByteCodes.dupN(mv, idNode.needDupChildrenCount() - 1);
                    }
                }
            }
            // for other nodes, visit via member-exp
            // when optimizing off, don't need to do this
            else if (compilingContext.option.treatGetterNoSideEffect) {
                // put on stack
                putActualIdOnStack(mv, idNode);
                invokeAccessorGetter(idNode.slotNo);
                // dup for each child
                // one more for access point
                ByteCodes.dupN(mv, idNode.needDupChildrenCount());

                // store access point
                idNode.variableIndex = ctx().nextVariableIndex();
                mv.visitVarInsn(ASTORE, idNode.variableIndex);
            }
        });
    }

    private void putActualIdOnStack(MethodVisitor mv, PropertyNode idNode) {
        var id = idNode.getActualIdentifer();
        mv.visitLdcInsn(id);

        if (id instanceof Integer) {
            ByteCodes.box(mv, Type.INT_TYPE);
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
            mv.visitLdcInsn(propertyNode.identifier);
            invokeAccessorGetter(propertyNode.slotNo);
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

    private void mathOpValConvert(MethodVisitor mv, ExpressionNode exp) {
        var valueType = exp.valueType;

        if (Types.isPrimitive(valueType.getType())) {
            ByteCodes.box(mv, valueType.getType());
        } else if (valueType != ValueType.STRING &&
            $.in(exp.type, AstType.BINARY_EXP, AstType.UNARY_EXP, AstType.COMMA_EXP)) {
            if (valueType == ValueType.STRING_BUILDER) {
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Object.class), "toString",
                    getMethodDescriptor(getType(String.class)), false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, getInternalName(JExpLang.class),
                    "stringBuilderToString",
                    getMethodDescriptor(getType(Object.class), getType(Object.class)),
                    false);
            }
        }
    }

    // function apply util
    private void applyFunction(String lib, String functionName,
                               List<ExpressionNode> args, boolean boxResult) {
        Optional<FunctionInfo> info = JExpFunctionFactory.getInfo(lib, functionName);
        if (!info.isPresent()) {
            throw new JExpCompilingException("function name not found: " + functionName);
        }

        applyFunction(info.get(), args, boxResult);
    }

    private void applyFunction(FunctionInfo info, List<ExpressionNode> args, boolean boxResult) {
        if (args.size() != info.method.getParameterCount()) {
            throw new JExpFunctionLoadException(info.name + " has " + info.method.getParameterCount() +
                " parameters, but give " + args.size());
        }

        // load stack
        int idx = 0;
        for (ExpressionNode astNode : args) {
            visit(astNode);
            ValueType vt = astNode.valueType;
            Class<?> parType = info.method.getParameterTypes()[idx++];

            if (vt == ValueType.BOOL) {
                if (parType != boolean.class) {
                    if (parType.isPrimitive()) {
                        throw new JExpFunctionArgumentConvertException(vt.getType().getClassName(), parType);
                    } else {
                        // wrap:
                        ByteCodes.box(mv, Type.BOOLEAN_TYPE);
                        // CHECKCAST on need
                        ByteCodes.ccOnNeed(mv, Boolean.class, parType);
                    }
                }
            } else if (vt == ValueType.BOOL_WRAPPED) {
                if (parType == boolean.class) {
                    // unbox
                    ByteCodes.unbox(mv, boolean.class);
                } else if (parType.isPrimitive()) {
                    throw new JExpFunctionArgumentConvertException(vt.getType().getClassName(), parType);
                } else {
                    ByteCodes.ccOnNeed(mv, Boolean.class, parType);
                }
            } else if (vt == ValueType.INT_WRAPPED) {
                if (parType == int.class) {
                    // unbox
                    ByteCodes.unbox(mv, int.class);
                } else if (parType.isPrimitive()) {
                    throw new JExpFunctionArgumentConvertException(vt.getType().getClassName(), parType);
                } else {
                    ByteCodes.ccOnNeed(mv, Integer.class, parType);
                }
            } else if (vt == ValueType.DOUBLE_WRAPPED) {
                if (parType == double.class) {
                    // unbox
                    ByteCodes.unbox(mv, double.class);
                } else if (parType.isPrimitive()) {
                    throw new JExpFunctionArgumentConvertException(vt.getType().getClassName(), parType);
                } else {
                    ByteCodes.ccOnNeed(mv, Double.class, parType);
                }
            }
            // other generic values
            else {
                if (parType.isPrimitive()) {
                    throw new JExpFunctionArgumentConvertException(vt.getType().getClassName(), parType);
                }

                // toString
                mathOpValConvert(mv, astNode);
                vt = ValueType.STRING;

                // check cast on need
                ByteCodes.ccOnNeed(mv, vt.getType().getClass(), parType);
            }
        }

        // call
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(info.method.getDeclaringClass()),
            info.method.getName(), getMethodDescriptor(info.method), false);

        if (info.method.getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        } else {
            if (boxResult) {
                // box values on need
                ByteCodes.box(mv, Type.getType(info.method.getReturnType()));
            }
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
        ctx.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
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
