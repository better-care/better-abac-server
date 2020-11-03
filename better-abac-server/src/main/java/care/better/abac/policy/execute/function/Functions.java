package care.better.abac.policy.execute.function;

import care.better.abac.policy.execute.Relation;
import care.better.abac.policy.execute.evaluation.EvaluationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Marker interface for component scan.
 *
 * @author Andrej Dolenc
 */
public interface Functions {

    static boolean isQueryParam(String parameter) {
        return EvaluationContext.QUERY_VALUE.equals(parameter);
    }

    static boolean isQueryParam(Collection<String> parameter) {
        return parameter.size() == 1 && parameter.stream().allMatch(Functions::isQueryParam);
    }

    static Collection<String> convertIds(Object ids) {
        if (ids instanceof Collection) {
            return (Collection<String>)ids;
        }
        if (ids instanceof Object[]) {
            return Arrays.stream((Object[])ids).map(Object::toString).collect(Collectors.toList());
        }
        if (ids instanceof String) {
            return Collections.singletonList((String)ids);
        }
        return Collections.emptyList();
    }

    static Collection<String> convertRelationNames(Object... relationNames) {
        return convertRelationChain(relationNames).stream().map(Relation::getName).collect(Collectors.toSet());
    }

    static List<Relation> convertRelationChain(Object... relationNames) {
        return Arrays.stream(relationNames).map(o -> (String)o).map(Relation::new).collect(Collectors.toList());
    }

    static List<Relation> reverse(List<Relation> relations) {

        List<Relation> reverse = relations.stream().map(r -> new Relation(r.getName(), !r.isInverse())).collect(Collectors.toList());
        Collections.reverse(reverse);
        return reverse;
    }
}
