package care.better.abac.policy;

import care.better.abac.audit.ExecutionAuditEntry;
import care.better.abac.audit.FunctionExecutionAuditEntry.Parameter;
import care.better.abac.audit.PolicyExecutionAuditor;
import care.better.abac.exception.PolicyExecutionException;
import care.better.abac.policy.antlr.PolicyLexer;
import care.better.abac.policy.antlr.PolicyParser;
import care.better.abac.policy.convert.ConvertingPolicyVisitor;
import care.better.abac.policy.definition.PolicyDefinition;
import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.PolicyExecutionContext;
import care.better.abac.policy.execute.PolicyHelper;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bostjan Lah
 */
public class PolicyTest {
    private final MyPolicyExecutionAuditor auditor = new MyPolicyExecutionAuditor();

    @Test
    public void policyAllow() throws IOException {
        PolicyDefinition policyDefinition = getPolicyDefinition("/policies/read.pdl");
        Map<String, Object> policyContext = getPolicyContext();

        assertThat(((BooleanEvaluationExpression)policyDefinition.evaluate("1", new PolicyExecutionContext(policyContext, new MyPolicyHelper(false)), auditor)).getBooleanValue()).isFalse();
        assertThat(auditor.getAuditLog()).hasSize(4);
        assertThat(auditor.getAuditLog().get(0))
                .hasFieldOrPropertyWithValue("functionName", "hasRelation")
                .hasFieldOrPropertyWithValue("parameters", Lists.newArrayList(new Parameter(0, "ctx.user", "admin"), new Parameter(1, "const", "PERSONAL_PHYSICIAN"), new Parameter(2,"ctx.patient", "123")))
                .extracting("result").hasFieldOrPropertyWithValue("booleanValue", false);
        assertThat(auditor.getAuditLog().get(1))
                .hasFieldOrPropertyWithValue("functionName", "hasRelation")
                .hasFieldOrPropertyWithValue("parameters", Lists.newArrayList(new Parameter(0, "ctx.user", "admin"), new Parameter(1, "const", "PERSON_OF_TRUST"), new Parameter(2, "ctx.patient", "123")))
                .extracting("result").hasFieldOrPropertyWithValue("booleanValue", false);
        assertThat(auditor.getAuditLog().get(3)).hasFieldOrPropertyWithValue("value", "DENY");
    }

    @Test
    public void policyDeny() throws IOException {
        PolicyDefinition policyDefinition = getPolicyDefinition("/policies/read.pdl");
        Map<String, Object> policyContext = getPolicyContext();

        assertThat(((BooleanEvaluationExpression)policyDefinition.evaluate("2", new PolicyExecutionContext(policyContext, new MyPolicyHelper(true)), auditor)).getBooleanValue()).isTrue();
        assertThat(auditor.getAuditLog()).hasSize(3);
        assertThat(auditor.getAuditLog().get(0))
                .hasFieldOrPropertyWithValue("functionName", "hasRelation")
                .hasFieldOrPropertyWithValue("parameters", Lists.newArrayList(new Parameter(0, "ctx.user", "admin"), new Parameter(1, "const", "PERSONAL_PHYSICIAN"), new Parameter(2, "ctx.patient", "123")))
                .extracting("result").hasFieldOrPropertyWithValue("booleanValue", false);
        assertThat(auditor.getAuditLog().get(1))
                .hasFieldOrPropertyWithValue("functionName", "hasRelation")
                .hasFieldOrPropertyWithValue("parameters", Lists.newArrayList(new Parameter(0, "ctx.user", "admin"), new Parameter(1, "const", "PERSON_OF_TRUST"), new Parameter(2,"ctx.patient", "123")))
                .extracting("result").hasFieldOrPropertyWithValue("booleanValue", true);
        assertThat(auditor.getAuditLog().get(2)).hasFieldOrPropertyWithValue("value", "ALLOW");
    }

    @Test
    public void policyError() throws IOException {
        PolicyDefinition policyDefinition = getPolicyDefinition("/policies/broken-read1.pdl");
        Map<String, Object> policyContext = getPolicyContext();

        try {
            policyDefinition.evaluate("2", new PolicyExecutionContext(policyContext, new MyPolicyHelper(true)), auditor);
        } catch (PolicyExecutionException ignored) {
        }

        assertThat(auditor.getAuditLog()).hasSize(1);
        assertThat(auditor.getAuditLog().get(0))
                .hasFieldOrPropertyWithValue("message", "Unable to evaluate function: 'xhasRelation'! [Function 'xhasRelation'not found!]");
    }

    private Map<String, Object> getPolicyContext() {
        Map<String, Object> policyContext = new HashMap<>();
        policyContext.put("user", "admin");
        policyContext.put("patient", "123");
        return policyContext;
    }

    private PolicyDefinition getPolicyDefinition(String name) throws IOException {
        PolicyLexer lexer = new PolicyLexer(new ANTLRInputStream(PolicyTest.class.getResourceAsStream(name)));
        PolicyParser parser = new PolicyParser(new CommonTokenStream(lexer));

        ConvertingPolicyVisitor visitor = new ConvertingPolicyVisitor();
        return visitor.convert(parser.policy());
    }

    private static final class MyPolicyHelper extends PolicyHelper {
        private MyPolicyHelper(boolean personOfTrust) {
            super(Lists.newArrayList(new MockHasRelationFunction(personOfTrust), new MockMatchesRelationFunction()), Collections.emptyList());
        }
    }

    private static class MyPolicyExecutionAuditor implements PolicyExecutionAuditor {
        private List<ExecutionAuditEntry> auditLog;

        @Override
        public void auditCall(String callId, List<ExecutionAuditEntry> auditLog) {
            this.auditLog = auditLog;
        }

        public List<ExecutionAuditEntry> getAuditLog() {
            return auditLog;
        }
    }

    private static final class MockHasRelationFunction extends ExecutableFunction {
        private final boolean personOfTrust;

        private MockHasRelationFunction(boolean personOfTrust) {
            this.personOfTrust = personOfTrust;
        }

        @Executable(type = Type.EVALUATE)
        public EvaluationExpression hasRelation(String sourceId, String relationName, Object targetIds) {
            if ("PERSONAL_PHYSICIAN".equals(relationName)) {
                return new BooleanEvaluationExpression(false);
            }
            if ("PERSON_OF_TRUST".equals(relationName)) {
                return new BooleanEvaluationExpression(personOfTrust);
            }
            return new BooleanEvaluationExpression(true);
        }

        @Executable(type = Type.QUERY)
        public EvaluationExpression queryRelation(String sourceId, String relationName, Object targetIds) {
            return new BooleanEvaluationExpression(true);
        }

        @Override
        public String getName() {
            return "hasRelation";
        }
    }

    private static final class MockMatchesRelationFunction extends ExecutableFunction {
        @Executable(type = Type.EVALUATE)
        public EvaluationExpression matchRelations(String sourceId, String sourceRelationName, String targetId, String targetRelationName) {
            return new BooleanEvaluationExpression(false);
        }

        @Executable(type = Type.QUERY)
        public EvaluationExpression queryMatchRelations(String sourceId, String sourceRelationName, String targetId, String targetRelationName) {
            return new BooleanEvaluationExpression(true);
        }

        @Override
        public String getName() {
            return "matchRelations";
        }
    }
}
