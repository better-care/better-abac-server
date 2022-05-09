package care.better.abac.rest;

import care.better.abac.oauth.SsoConfiguration;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationContext;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static care.better.abac.policy.execute.PolicyHelper.OAUTH2_TOKEN_ATTRIBUTE_EXTRACTOR;

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

    @Autowired(required = false)
    private SsoConfiguration sso;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/execute/name/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> executeByNameSimple(@PathVariable("name") String name, @RequestBody Map<String, Object> ctx) {
        log.debug("Executing policy {} with simple boolean result, ctx={}", name, ctx);
        EvaluationExpression expression = pdlPolicyService.executeByName(name, createContext(ctx).getContext());
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
        EvaluationExpression result = pdlPolicyService.queryByName(name, createContext(ctx).getContext());
        if (log.isTraceEnabled()) {
            //noinspection OverlyBroadCatchBlock
            try {
                log.trace("Result of policy {} execution, ctx={}, result={}", name, ctx, objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                log.trace("Result of policy {} execution, ctx={}, result cannot be serialized ({})", name, ctx, e.getMessage());
            }
        }
        return result;
    }

    @RequestMapping(value = "/execute/name/{name}/expression/multi", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<EvaluationExpression> executeMultiByNameComplex(@PathVariable("name") String name, @RequestBody List<Map<String, Object>> ctx) {
        return ctx.stream().map(it -> {
            log.debug("Executing policy {} with expression result, ctx={}", name, it);
            return pdlPolicyService.queryByName(name, createContext(it).getContext());
        }).collect(Collectors.toList());
    }

    private EvaluationContext createContext(Map<String, Object> ctx) {
        EvaluationContext context = new EvaluationContext(ctx);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (!context.isQueryValue(EvaluationContext.USER_KEY)) {
                context.setContextValue(EvaluationContext.USER_KEY, authentication.getName());
            }
            if (sso != null) {
                sso.getAbacContextMapping().stream()
                        .filter(am -> !context.isQueryValue(am.getContextKey()))
                        .forEach(am -> {
                            Object attributeValue = OAUTH2_TOKEN_ATTRIBUTE_EXTRACTOR.apply(authentication, am.getTokenAttributePath());
                            if (attributeValue != null) {
                                if (attributeValue instanceof Collection<?>) {
                                    if (!((Collection<?>)attributeValue).isEmpty()) {
                                        context.setContextValue(am.getContextKey(), attributeValue);
                                    }
                                } else if (attributeValue instanceof String) {
                                    if (StringUtils.isNotBlank((CharSequence)attributeValue)) {
                                        context.setContextValue(am.getContextKey(), attributeValue);
                                    }
                                } else {
                                    context.setContextValue(am.getContextKey(), attributeValue);
                                }
                            }
                        });
            }
        }
        return context;
    }
}
