package care.better.abac.policy.execute.function;

import care.better.abac.oauth.SecurityHelper;
import care.better.abac.policy.execute.Executable;
import care.better.abac.policy.execute.Executable.Type;
import care.better.abac.policy.execute.ExecutableFunction;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Andrej Dolenc
 */
@Component
public class IsAuthenticatedFunction extends ExecutableFunction {
    @Autowired(required = false)
    private SecurityHelper securityHelper;

    @Executable(type = Type.EVALUATE)
    public EvaluationExpression isAuthenticatedEvaluate() {
        return new BooleanEvaluationExpression(securityHelper != null && securityHelper.isAuthenticated());
    }

    @Executable(type = Type.QUERY)
    public EvaluationExpression isAuthenticatedQuery() {
        return isAuthenticatedEvaluate();
    }
}