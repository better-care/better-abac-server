package care.better.abac.policy.config;

import care.better.abac.audit.PolicyExecutionAuditor;
import care.better.abac.jpa.repo.PolicyRepository;
import care.better.abac.policy.execute.ExecutableConversion;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.PolicyHelper;
import care.better.abac.policy.execute.conversion.Conversions;
import care.better.abac.policy.execute.function.Functions;
import care.better.abac.policy.service.PolicyService;
import care.better.abac.policy.service.impl.PDLPolicyService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Andrej Dolenc
 */
@Configuration
@ComponentScan(basePackageClasses = {Functions.class, Conversions.class})
public class PolicyConfiguration {

    @Bean
    public PolicyHelper policyHelper(@NonNull List<ExecutableFunction> executableFunctions, @NonNull List<ExecutableConversion> executableConversions) {
        return new PolicyHelper(executableFunctions, executableConversions);
    }

    @Bean
    public PolicyService policyService(
            @NonNull PolicyHelper policyHelper,
            @NonNull PolicyRepository policyRepository,
            @NonNull PolicyExecutionAuditor policyExecutionAuditor,
            @Value("${abac.policyRefreshPeriodInMs:5000}") long policyRefreshPeriodInMs) {
        return new PDLPolicyService(policyHelper, policyRepository, policyExecutionAuditor, policyRefreshPeriodInMs);
    }
}
