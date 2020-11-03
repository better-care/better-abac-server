package care.better.abac.external;

import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
@Component
public class SetKeyGenerator extends SimpleKeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params[0] instanceof Set) {
            Set<String> set = (Set<String>)params[0];
            if (set.size() > 1) {
                List<String> list = new ArrayList<>(set);
                Collections.sort(list);
                return String.join("|", list);
            } else if (set.isEmpty()) {
                return SimpleKey.EMPTY;
            } else {
                return set.iterator().next();
            }
        }
        return super.generate(target, method, params);
    }
}
