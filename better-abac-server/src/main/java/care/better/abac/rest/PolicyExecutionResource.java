package care.better.abac.rest;

import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.service.PolicyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bostjan Lah
 */
@Component
@RestController
@RequestMapping("/rest/v1/policy")
@Transactional
public class PolicyExecutionResource {
    private static final Logger log = LogManager.getLogger(PolicyExecutionResource.class.getName());

    private static final String ERROR_DESCRIPTION = "error_description";

    @Autowired
    private PolicyService pdlPolicyService;

    @RequestMapping(value = "/execute/name/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> executeByNameSimple(@PathVariable("name") String name, @RequestBody Map<String, Object> ctx) {
        log.debug("Executing policy {} with simple boolean result, ctx={}", name, ctx);
        EvaluationExpression expression = evaluate(name, createContext(ctx));
        if (expression instanceof BooleanEvaluationExpression) {
            if (((BooleanEvaluationExpression)expression).getBooleanValue()) {
                return ResponseEntity.ok().build();
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put(ERROR_DESCRIPTION, "Not Authorized!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put(ERROR_DESCRIPTION, "Policy execution did not return simple boolean condition!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        }
    }

    @RequestMapping(value = "/execute/name/{name}/expression", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public EvaluationExpression executeByNameComplex(@PathVariable("name") String name, @RequestBody Map<String, Object> ctx) {
        log.debug("Executing policy {} with expression result, ctx={}", name, ctx);
        return evaluate(name, createContext(ctx));
    }

    private EvaluationContext createContext(Map<String, Object> ctx) {
        EvaluationContext context = new EvaluationContext(ctx);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && !context.isQueryValue(EvaluationContext.USER_KEY)) {
            context.setContextValue(EvaluationContext.USER_KEY, authentication.getName());
        }
        return context;
    }

    private EvaluationExpression evaluate(String policyName, EvaluationContext context) {
        return context.hasQuery()
                ? pdlPolicyService.queryByName(policyName, context.getContext())
                : pdlPolicyService.executeByName(policyName, context.getContext());
    }
}
