package care.better.abac.policy.convert;

import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.policy.antlr.PolicyBaseVisitor;
import care.better.abac.policy.antlr.PolicyParser;
import care.better.abac.policy.antlr.PolicyParser.ConstantContext;
import care.better.abac.policy.antlr.PolicyParser.ConversionArgumentsContext;
import care.better.abac.policy.antlr.PolicyParser.ConversionContext;
import care.better.abac.policy.antlr.PolicyParser.DecisionContext;
import care.better.abac.policy.antlr.PolicyParser.FunctionContext;
import care.better.abac.policy.antlr.PolicyParser.OperationContext;
import care.better.abac.policy.antlr.PolicyParser.PolicyContext;
import care.better.abac.policy.antlr.PolicyParser.QuantifierContext;
import care.better.abac.policy.antlr.PolicyParser.VariableContext;
import care.better.abac.policy.definition.DecisionConversion;
import care.better.abac.policy.definition.DecisionFunction;
import care.better.abac.policy.definition.DecisionOperation;
import care.better.abac.policy.definition.DecisionOperation.Quantifier;
import care.better.abac.policy.definition.DecisionPolicyRule;
import care.better.abac.policy.definition.PolicyDefinition;
import care.better.abac.policy.definition.PolicyFunctionConstantParameter;
import care.better.abac.policy.definition.PolicyFunctionParameter;
import care.better.abac.policy.definition.PolicyFunctionVariableParameter;
import care.better.abac.policy.definition.PolicyRule;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Bostjan Lah
 */
public final class ConvertingPolicyVisitor extends PolicyBaseVisitor<PolicyRule> {
    private final ParseTreeProperty<List<PolicyRule>> operationsProperty = new ParseTreeProperty<>();
    private PolicyFunctionParameter[] parameters;
    private int parameterIndex = 0;

    public PolicyDefinition convert(PolicyContext ctx) {
        return new PolicyDefinition(visitPolicy(ctx));
    }

    @Override
    public PolicyRule visitPolicy(PolicyContext ctx) {
        DecisionContext decisionContext = ctx.decision();
        return new DecisionPolicyRule(visitOperation(decisionContext.operation()));
    }

    @Override
    public PolicyRule visitOperation(OperationContext ctx) {
        List<PolicyRule> operations = operationsProperty.get(ctx.getParent());
        PolicyRule policyRule = convertOperation(ctx);
        if (operations != null) {
            operations.add(policyRule);
        }
        return policyRule;
    }

    @Override
    public PolicyRule visitConversion(ConversionContext ctx) {
        ConversionArgumentsContext conversionArguments = ctx.conversionArguments();
        FunctionContext functionContext = conversionArguments.function();
        OperationContext operationContext = conversionArguments.operation();
        ConversionContext conversionContext = conversionArguments.conversion();
        String conversionName = ctx.conversionName().getText();
        PolicyFunctionParameter[] conversionParameters = extractArguments(conversionArguments.argument());
        if (operationContext != null)
        {
            PolicyRule operation = visitOperation(operationContext);
            return new DecisionConversion(conversionName, operation, conversionParameters);
        }
        else if (functionContext != null) {
            PolicyRule function = visitFunction(functionContext);
            return new DecisionConversion(conversionName, function, conversionParameters);
        } else if (conversionContext != null) {
            return new DecisionConversion(conversionName, visitConversion(conversionContext), conversionParameters);
        } else {
            throw new PolicyExecutionException("Expected function, conversion or operation, got none!");
        }
    }

    private PolicyFunctionParameter[] extractArguments(List<PolicyParser.ArgumentContext> argument) {
        parameters = new PolicyFunctionParameter[argument.size()];
        parameterIndex = 0;
        argument.forEach(this::visitArgument);
        return parameters;
    }

    @Override
    public PolicyRule visitFunction(FunctionContext ctx) {
        String functionName = ctx.functionName().getText();

        PolicyFunctionParameter[] functionParameters = extractArguments(ctx.arguments().argument());
        return new DecisionFunction(functionName, functionParameters);
    }

    @Override
    public PolicyRule visitConstant(ConstantContext ctx) {
        parameters[parameterIndex++] = new PolicyFunctionConstantParameter(unquote(ctx.getText()));
        return null;
    }

    @Override
    public PolicyRule visitVariable(VariableContext ctx) {
        parameters[parameterIndex++] = new PolicyFunctionVariableParameter(ctx.getText());
        return null;
    }

    private PolicyRule convertOperation(OperationContext ctx) {
        QuantifierContext quantifierContext = ctx.quantifier();
        FunctionContext functionContext = ctx.function();
        ConversionContext conversionContext = ctx.conversion();
        if (functionContext != null) {
            return visitFunction(functionContext);
        } else if (conversionContext != null) {
            return visitConversion(conversionContext);
        } else if (quantifierContext != null) {
            List<PolicyRule> operations = new LinkedList<>();
            operationsProperty.put(ctx.operations(), operations);
            visitChildren(ctx);
            return new DecisionOperation(Quantifier.valueOf(quantifierContext.getText()), operations);
        } else {
            throw new PolicyExecutionException("Expected function, conversion or quantifier context, got none!");
        }
    }

    /**
     * Remove single quote at the start and end of a string
     *
     * @param text string which might be in quotes
     * @return unquoted string
     */
    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    public static String unquote(String text) {
        if (StringUtils.isNotEmpty(text)) {
            return unquoteLeft(unquoteRight(text)).replace("\\'", "'").replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return text;
    }


    private static String unquoteLeft(String text) {
        if (text.startsWith("'") || text.startsWith("\"")) {
            return text.substring(1);
        }
        return text;
    }

    private static String unquoteRight(String text) {
        if (text.endsWith("'") || text.endsWith("\"")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }
}
