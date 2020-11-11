/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.AstUtil;
import ranttu.rapid.jexp.common.Types;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.closure.NameClosure;
import ranttu.rapid.jexp.compile.closure.PropertyNode;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.AstType;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
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
import ranttu.rapid.jexp.compile.parse.ast.PropertyAccessNode;
import ranttu.rapid.jexp.exception.TooManyLinqRangeVariables;
import ranttu.rapid.jexp.exception.UnknownFunction;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.indy.JExpIndyFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * do some prepare jobs
 * include:
 * type inferring
 * member expression folding
 * constant folding
 * build identifier tree
 *
 * @author dongwei.dq
 * @version $Id: TypeInferPass.java, v0.1 2017-08-24 6:06 PM dongwei.dq Exp $
 */
public class PreparePass extends NoReturnPass<PreparePass.PrepareContext> {
    @Override
    protected void prepare() {
        compilingContext.names = NameClosure.root();
        var ctx = newCtx(compilingContext.names);
        ctxStack.push(ctx);
    }

    @Override
    protected void visit(PrimaryExpression primary) {
        var t = primary.token;

        switch (t.type) {
            case STRING:
                primary.isConstant = true;
                primary.valueType = Type.getType(String.class);
                primary.constantValue = t.getString();
                return;
            case INTEGER:
                primary.isConstant = true;
                primary.valueType = Type.INT_TYPE;
                primary.constantValue = t.getInt();
                return;
            case FLOAT:
                primary.isConstant = true;
                primary.valueType = Type.DOUBLE_TYPE;
                primary.constantValue = t.getDouble();
                return;
            case IDENTIFIER:
                // i.e. a direct identifier load
                primary.isConstant = false;
                primary.valueType = Type.getType(Object.class);

                // build id tree
                primary.isStatic = true;
                primary.propertyNode = names().addNameAccess(AstUtil.asId(primary));
                return;
            default:
                $.notSupport(t.type);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void visit(BinaryExpression exp) {
        visit(exp.left);
        visit(exp.right);

        //~~~ infer ret type
        // for String
        if (exp.op.is(TokenType.PLUS)
            && (Types.isString(exp.left.valueType) || Types.isString(exp.right.valueType))) {
            exp.valueType = Types.JEXP_STRING;
        }
        // for cond
        else if (exp.op.is(TokenType.OR) || exp.op.is(TokenType.AND)) {
            exp.valueType = Types.JEXP_GENERIC;
        }
        // comparator
        else if ($.in(exp.op.type, TokenType.EQEQ, TokenType.NOT_EQ, TokenType.GREATER,
            TokenType.GREATER_EQ, TokenType.SMALLER, TokenType.SMALLER_EQ)) {
            exp.valueType = Types.JEXP_BOOL;
        }
        // number
        else {
            if ($.notIn(exp.op.type, TokenType.PLUS, TokenType.SUBTRACT, TokenType.MULTIPLY,
                TokenType.DIVIDE, TokenType.MODULAR)) {
                $.notSupport("unknown binary op: " + exp.op);
            }

            // TODO: @dongwei.dq, refine for wrapper types
            if (!Types.isNumber(exp.left.valueType) || !Types.isNumber(exp.right.valueType)) {
                // i.e., determine at runtime
                exp.valueType = Types.JEXP_GENERIC;
            } else {
                if (Types.isFloat(exp.left.valueType) || Types.isFloat(exp.right.valueType)) {
                    exp.valueType = Types.JEXP_FLOAT;
                } else {
                    exp.valueType = Types.JEXP_INT;
                }
            }
        }

        //~~~ calc constant
        if (exp.left.isConstant && exp.right.isConstant) {
            exp.isConstant = true;

            // for cond
            if (exp.op.is(TokenType.OR) || exp.op.is(TokenType.AND)) {
                var leftValue = exp.left.constantValue;
                var rightValue = exp.right.constantValue;

                switch (exp.op.type) {
                    case OR:
                        exp.constantValue = JExpLang.exactBoolean(leftValue) ? leftValue : rightValue;
                        break;
                    case AND:
                        exp.constantValue = JExpLang.exactBoolean(leftValue) ? rightValue : leftValue;
                        break;
                }

                // re infer the type
                if (exp.constantValue == null) {
                    exp.valueType = Types.JEXP_GENERIC;
                } else {
                    exp.valueType = Type.getType(exp.constantValue.getClass());
                }
            }
            // comparators
            else if ($.in(exp.op, TokenType.EQEQ, TokenType.NOT_EQ)) {
                var leftValue = exp.left.constantValue;
                var rightValue = exp.right.constantValue;

                switch (exp.op.type) {
                    case EQEQ:
                        exp.constantValue = JExpLang.eq(leftValue, rightValue);
                        break;
                    case NOT_EQ:
                        exp.constantValue = !JExpLang.eq(leftValue, rightValue);
                        break;
                }
            }
            // for math
            else {
                if (Types.isString(exp.valueType)) {
                    exp.constantValue = exp.left.stringConstant() + exp.right.stringConstant();
                } else if (Types.isFloat(exp.valueType)) {
                    var leftValue = exp.left.floatConstant();
                    var rightValue = exp.right.floatConstant();

                    switch (exp.op.type) {
                        case PLUS:
                            exp.constantValue = leftValue + rightValue;
                            break;
                        case SUBTRACT:
                            exp.constantValue = leftValue - rightValue;
                            break;
                        case MULTIPLY:
                            exp.constantValue = leftValue * rightValue;
                            break;
                        case DIVIDE:
                            exp.constantValue = leftValue / rightValue;
                            break;
                        case MODULAR:
                            exp.constantValue = leftValue % rightValue;
                            break;
                        case GREATER:
                            exp.constantValue = leftValue > rightValue;
                            break;
                        case GREATER_EQ:
                            exp.constantValue = leftValue >= rightValue;
                            break;
                        case SMALLER:
                            exp.constantValue = leftValue < rightValue;
                            break;
                        case SMALLER_EQ:
                            exp.constantValue = leftValue <= rightValue;
                            break;
                    }
                } else {
                    int leftValue = exp.left.intConstant();
                    int rightValue = exp.right.intConstant();

                    switch (exp.op.type) {
                        case PLUS:
                            exp.constantValue = leftValue + rightValue;
                            break;
                        case SUBTRACT:
                            exp.constantValue = leftValue - rightValue;
                            break;
                        case MULTIPLY:
                            exp.constantValue = leftValue * rightValue;
                            break;
                        case DIVIDE:
                            exp.constantValue = leftValue / rightValue;
                            break;
                        case MODULAR:
                            exp.constantValue = leftValue % rightValue;
                            break;
                        case GREATER:
                            exp.constantValue = leftValue > rightValue;
                            break;
                        case GREATER_EQ:
                            exp.constantValue = leftValue >= rightValue;
                            break;
                        case SMALLER:
                            exp.constantValue = leftValue < rightValue;
                            break;
                        case SMALLER_EQ:
                            exp.constantValue = leftValue <= rightValue;
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void visit(CallExpression func) {
        //~~~ no lib direct call
        if (AstUtil.isIdentifier(func.caller)) {
            var functionName = AstUtil.asId(func.caller);
            var infoOptional = JExpFunctionFactory.getInfo(functionName);

            if (infoOptional.isPresent()) {
                prepareUnboundedInvoke(func, infoOptional.get());
            } else {
                throw new UnknownFunction(functionName);
            }

        }
        //~~~ lib direct call
        else if (!asLibInvoke(func)) {
            //~~~ bounded call
            prepareBoundedInvoke(func);
        }
    }

    private boolean asLibInvoke(CallExpression func) {
        if (func.caller.is(AstType.MEMBER_EXP)) {
            var callerMember = (MemberExpression) func.caller;

            String libName, funcName;
            // get lib name
            if (AstUtil.isIdentifier(callerMember.owner)) {
                libName = AstUtil.asId(callerMember.owner);
            } else {
                return false;
            }

            // get func name
            if (AstUtil.isExactString(callerMember.propertyName)) {
                funcName = AstUtil.asExactString(callerMember.propertyName);
            } else {
                return false;
            }

            // get function info
            var infoOptional = JExpFunctionFactory.getInfo(libName, funcName);
            if (infoOptional.isPresent()) {
                prepareUnboundedInvoke(func, infoOptional.get());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void prepareBoundedInvoke(CallExpression func) {
        $.should(func.caller.is(AstType.MEMBER_EXP));
        var callerMember = (MemberExpression) func.caller;

        $.should(AstUtil.isExactString(callerMember.propertyName));
        visit(callerMember.owner);

        // visit all parameters
        for (ExpressionNode astNode : func.parameters) {
            visit(astNode);
        }

        // TODO: this is to tricky
        func.caller = callerMember.owner;

        func.isBounded = true;
        func.slotNo = JExpIndyFactory.nextSlotNo();
        func.isConstant = false;
        func.valueType = Type.getType(Object.class);
        func.methodName = AstUtil.asExactString(callerMember.propertyName);
    }

    private void prepareUnboundedInvoke(CallExpression func, FunctionInfo info) {
        // visit all parameters
        for (ExpressionNode astNode : func.parameters) {
            visit(astNode);
        }

        // cannot infer constant value for function expressions now
        func.isBounded = false;
        func.functionInfo = info;
        func.isConstant = false;
        func.valueType = Type.getType(info.method.getReturnType());
    }

    @Override
    protected void visit(MemberExpression member) {
        visit(member.owner);
        visit(member.propertyName);

        // member expression fold
        if (member.propertyName.isConstant && member.owner instanceof PropertyAccessNode) {
            var owner = (PropertyAccessNode) member.owner;

            if (owner.isStatic) {
                member.isStatic = true;
            }
        }

        if (member.owner instanceof PropertyAccessNode) {
            var owner = (PropertyAccessNode) member.owner;
            if (owner.propertyNode != null) {
                member.propertyNode = names().addNameAccess(owner.propertyNode,
                    AstUtil.asConstantString(member.propertyName));
            }
        }

        if (member.propertyNode == null) {
            // set slotNo
            member.slotNo = JExpIndyFactory.nextSlotNo();
        }

        // currently member expression is always not a constant
        member.isConstant = false;
        member.valueType = Types.JEXP_GENERIC;
    }

    @Override
    protected void visit(ArrayExpression exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_ARRAY;

        exp.items.forEach(this::visit);
    }

    @Override
    protected void visit(LambdaExpression exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        // construct names
        var names = NameClosure.independent(names());
        var idx = 0;
        for (var parId : exp.parameters) {
            var node = names.declareName(parId);
            node.functionParameter = true;
            node.functionParameterIndex = idx++;
        }
        exp.names = names;

        in(newCtx(names), () -> {
            visit(exp.body);
            return null;
        });
    }

    @Override
    protected void visit(LinqExpression exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        ((LinqFromClause) exp.queryBodyClauses.get(0)).firstFromClause = true;

        // construct names
        var names = NameClosure.embedded(names());
        exp.names = names;
        in(newCtx(names), () -> {
            exp.queryBodyClauses.forEach(this::visit);
            visit(exp.finalQueryClause);
            return null;
        });

        // don't share parents name closure
        prepareForFinalCont(exp.finalQueryClause);
    }

    @Override
    protected void visit(LinqFromClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        if (exp.firstFromClause) {
            visit(exp.sourceExp);
        } else {
            exp.sourceLambda = defineLinqLambda(exp.sourceExp);
            exp.isSourceStatic = true;

            for (var id : exp.sourceLambda.parameters) {
                var node = exp.sourceLambda.names.getLocalName(id);
                if (node.accessedFromLocalOrChildren) {
                    exp.isSourceStatic = false;
                    break;
                }
            }
        }

        var node = declareLinqParameter(exp.itemName);
        exp.linqParameterIndex = node.linqParameterIndex;
    }

    @Override
    protected void visit(LinqLetClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        exp.lambdaExp = defineLinqLambda(exp.sourceExp);

        var node = declareLinqParameter(exp.itemName);
        exp.linqParameterIndex = node.linqParameterIndex;
    }

    @Override
    protected void visit(LinqWhereClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        exp.lambdaExp = defineLinqLambda(exp.whereExp);
    }

    @Override
    protected void visit(LinqSelectClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        exp.lambdaExp = defineLinqLambda(exp.selectExp);
    }


    @Override
    protected void visit(LinqGroupByClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        exp.selectLambda = defineLinqLambda(exp.selectExp);
        exp.keyLambda = defineLinqLambda(exp.keyExp);
    }


    private void prepareForFinalCont(LinqFinalQueryClause exp) {
        if (!exp.hasQueryContinuation()) {
            return;
        }

        var names = NameClosure.embedded(names());
        exp.names = names;

        in(newCtx(names), () -> {
            var node = declareLinqParameter(exp.contItemName);
            exp.contItemLinqParameterIndex = node.linqParameterIndex;

            exp.queryBodyClauses.forEach(this::visit);
            visit(exp.finalQueryClause);
            return null;
        });
    }

    @Override
    protected void visit(LinqOrderByClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        for (var item : exp.items) {
            item.keySelectLambda = defineLinqLambda(item.exp);
        }
    }

    @Override
    protected void visit(LinqJoinClause exp) {
        exp.isConstant = false;
        exp.valueType = Types.JEXP_GENERIC;

        // won't limit left/right on equals operation

        // source
        visit(exp.sourceExp);

        // outer key lambda
        exp.outerKeyLambda = defineLinqLambda(exp.outerKeyExp);

        // inner key enter namespace
        var node = declareLinqParameter(exp.innerItemName);
        exp.innerItemLinqParameterIndex = node.linqParameterIndex;

        // inner key lambda
        exp.innerKeyLambda = defineLinqLambda(exp.innerKeyExp);

        if (exp.isGroupJoin()) {
            var groupJoinNode = declareLinqParameter(exp.groupJoinItemName);
            exp.groupJoinItemLinqParameterIndex = groupJoinNode.linqParameterIndex;
        }
    }

    //~~ ctx helpers
    private NameClosure names() {
        return ctx().names;
    }

    private PropertyNode declareLinqParameter(String name) {
        var node = names().declareName(name);
        node.linqParameter = true;
        node.linqParameterIndex = ctx().nextLinqVariableIndex();

        return node;
    }

    private LambdaExpression defineLinqLambda(ExpressionNode body) {
        // collect parameters
        var parameterProperties = getAllLinqParametersInOrder();
        var parIds = parameterProperties.stream()
            .map(p -> p.identifier).collect(Collectors.toList());

        var lambda = new LambdaExpression(parIds, body);
        visit(lambda);

        return lambda;
    }

    private List<PropertyNode> getAllLinqParametersInOrder() {
        return ctx().names.getLocalNames().stream()
            .filter(p -> p.linqParameter)
            .sorted(Comparator.comparingInt(o -> o.linqParameterIndex))
            .collect(Collectors.toList());
    }

    private PrepareContext newCtx(NameClosure names) {
        var ctx = new PrepareContext();
        ctx.names = names;
        return ctx;
    }

    class PrepareContext {
        public NameClosure names;

        public int linqVariableCount = 0;

        public int nextLinqVariableIndex() {
            if (linqVariableCount >= CompileOption.MAX_LINQ_PARS) {
                throw new TooManyLinqRangeVariables();
            }

            return linqVariableCount++;
        }
    }
}
