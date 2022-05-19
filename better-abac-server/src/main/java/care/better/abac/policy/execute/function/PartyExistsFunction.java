package care.better.abac.policy.execute.function;

import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Primoz Delopst
 */
@Component
public class PartyExistsFunction extends ExecutableFunction {
    @Resource
    private PartyRepository partyRepository;

    @Executable(type = Executable.Type.EVALUATE)
    public EvaluationExpression partyExistsEvaluate(String type, String externalId) {
        return new BooleanEvaluationExpression(partyRepository.findByTypeAndExternalId(type, externalId) != null);
    }

    @Executable(type = Executable.Type.QUERY)
    public EvaluationExpression partyExistsQuery(String type, String name) {
        return Functions.isQueryParam(name)
                ? ResultSetEvaluationExpression.create(partyRepository.findExternalIdsByType(type))
                : partyExistsEvaluate(type, name);
    }
}
