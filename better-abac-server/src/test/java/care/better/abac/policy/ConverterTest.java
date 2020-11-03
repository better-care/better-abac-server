package care.better.abac.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import care.better.abac.policy.antlr.PolicyLexer;
import care.better.abac.policy.antlr.PolicyParser;
import care.better.abac.policy.convert.ConvertingPolicyVisitor;
import care.better.abac.policy.definition.DecisionFunction;
import care.better.abac.policy.definition.DecisionOperation;
import care.better.abac.policy.definition.DecisionPolicyRule;
import care.better.abac.policy.definition.PolicyDefinition;
import care.better.abac.policy.definition.PolicyRule;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static care.better.abac.policy.definition.DecisionOperation.Quantifier;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bostjan Lah
 */
public class ConverterTest {
    @Test
    public void singleFunction() throws IOException {
        PolicyDefinition policyDefinition = convert("f()");

        PolicyRule policyRule = policyDefinition.getPolicyRule();
        assertThat(policyRule).isInstanceOf(DecisionPolicyRule.class);

        DecisionPolicyRule decisionPolicyRule = (DecisionPolicyRule)policyRule;
        assertThat(decisionPolicyRule.getOperation()).isInstanceOf(DecisionFunction.class);
    }

    @Test
    public void nestedMultipleOperations() throws IOException {
        PolicyDefinition policyDefinition = convert("ALL_OF(f1(), ANY_OF(f2()))");

        PolicyRule policyRule = policyDefinition.getPolicyRule();
        assertThat(policyRule).isInstanceOf(DecisionPolicyRule.class);

        DecisionPolicyRule decisionPolicyRule = (DecisionPolicyRule)policyRule;
        assertThat(decisionPolicyRule.getOperation()).isInstanceOf(DecisionOperation.class).hasFieldOrPropertyWithValue("quantifier", Quantifier.ALL_OF);
        DecisionOperation operation = (DecisionOperation)decisionPolicyRule.getOperation();
        assertThat(operation.getOperations().size()).isEqualTo(2);
        assertThat(operation.getOperations().get(0)).isInstanceOf(DecisionFunction.class).hasFieldOrPropertyWithValue("functionName", "f1");
        assertThat(operation.getOperations().get(1)).isInstanceOf(DecisionOperation.class).hasFieldOrPropertyWithValue("quantifier", Quantifier.ANY_OF);
        DecisionOperation operation2 = (DecisionOperation)operation.getOperations().get(1);
        assertThat(operation2.getOperations().size()).isEqualTo(1);
        assertThat(operation2.getOperations().get(0)).isInstanceOf(DecisionFunction.class).hasFieldOrPropertyWithValue("functionName", "f2");

    }

    @Test
    public void policy() throws IOException {
        PolicyLexer lexer = new PolicyLexer(new ANTLRInputStream(ConverterTest.class.getResourceAsStream("/policies/read.pdl")));
        PolicyParser parser = new PolicyParser(new CommonTokenStream(lexer));

        ConvertingPolicyVisitor visitor = new ConvertingPolicyVisitor();
        PolicyDefinition policyDefinition = visitor.convert(parser.policy());

        PolicyRule policyRule = policyDefinition.getPolicyRule();
        assertThat(policyRule).isInstanceOf(DecisionPolicyRule.class);

        DecisionPolicyRule decisionPolicyRule = (DecisionPolicyRule)policyRule;
        assertThat(decisionPolicyRule.getOperation()).isInstanceOf(DecisionOperation.class).hasFieldOrPropertyWithValue("quantifier", Quantifier.ANY_OF);
        DecisionOperation decisionOperation = (DecisionOperation)decisionPolicyRule.getOperation();
        assertThat(decisionOperation.getOperations()).filteredOn(f -> "hasRelation".equals(((DecisionFunction)f).getFunctionName())).hasSize(2);
        assertThat(decisionOperation.getOperations()).filteredOn(f -> "matchRelations".equals(((DecisionFunction)f).getFunctionName())).hasSize(1);
    }

    @Test
    public void policySerialized() throws IOException {
        PolicyLexer lexer = new PolicyLexer(new ANTLRInputStream(ConverterTest.class.getResourceAsStream("/policies/read1.pdl")));
        PolicyParser parser = new PolicyParser(new CommonTokenStream(lexer));

        ConvertingPolicyVisitor visitor = new ConvertingPolicyVisitor();
        PolicyDefinition policyDefinition = visitor.convert(parser.policy());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator());

        StringWriter writer = new StringWriter();

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, policyDefinition);

        PolicyDefinition policyDefinition1 = objectMapper.readValue(writer.toString(), PolicyDefinition.class);
        assertThat(policyDefinition.getPolicyRule()).isNotNull();
        assertThat(policyDefinition1.getPolicyRule()).isNotNull();
    }

    private PolicyDefinition convert(String policyString) {
        PolicyLexer lexer = new PolicyLexer(new ANTLRInputStream(policyString));
        PolicyParser parser = new PolicyParser(new CommonTokenStream(lexer));

        ConvertingPolicyVisitor visitor = new ConvertingPolicyVisitor();
        return visitor.convert(parser.policy());
    }
}
