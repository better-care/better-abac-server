package care.better.abac.policy.execute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;

/**
 * @author Andrej Dolenc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executable {
    EnumSet<Type> FUNCTION_TYPES = EnumSet.of(Type.EVALUATE, Type.QUERY);

    EnumSet<Type> CONVERSION_TYPES = EnumSet.of(Type.CONVERT);

    enum Type {EVALUATE, QUERY, CONVERT}

    Type type();
}
