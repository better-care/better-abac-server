package care.better.abac.plugin.condition;

import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.spi.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Andrej Dolenc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnBean(PluginManager.class)
@Conditional(OnServiceTypeCondition.class)
public @interface ConditionalOnServiceType {
    /**
     * The class types of services that should be checked. The condition matches when at least one
     * service of the specified type exists and is initialized by {@link PluginManager}.
     *
     * @return the class types of services to check
     */
    Class<? extends Service> value();
}
