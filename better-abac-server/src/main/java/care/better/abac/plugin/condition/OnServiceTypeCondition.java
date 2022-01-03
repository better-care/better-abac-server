package care.better.abac.plugin.condition;

import care.better.core.Opt;
import care.better.abac.plugin.PluginManager;
import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.spi.Service;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;
import java.util.Objects;

/**
 * @author Andrej Dolenc
 */
@Order
public class OnServiceTypeCondition extends SpringBootCondition implements ConfigurationCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public ConditionOutcome getMatchOutcome(
            ConditionContext context, AnnotatedTypeMetadata metadata) {
        MergedAnnotations annotations = metadata.getAnnotations();

        if (annotations.isPresent(ConditionalOnServiceType.class)) {
            PluginManager manager;
            try {
                manager = Objects.requireNonNull(context.getBeanFactory()).getBean(PluginManager.class);
            } catch (NoSuchBeanDefinitionException e) {
                manager = null;
            }

            if (manager == null) {
                return ConditionOutcome.noMatch(ConditionMessage.empty());
            }
            Class<? extends Service> serviceType = annotations.get(ConditionalOnServiceType.class).synthesize().value();
            Map<Key, ? extends Service> servicesOfType = manager.getServicesOfType(serviceType);
            return servicesOfType.isEmpty() ? ConditionOutcome.noMatch(
                    ConditionMessage.forCondition(ConditionalOnServiceType.class)
                            .didNotFind(String.format("No services of type %s found!", serviceType.getName()))
                            .atAll()) : ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch(ConditionMessage.empty());
        }
    }
}
