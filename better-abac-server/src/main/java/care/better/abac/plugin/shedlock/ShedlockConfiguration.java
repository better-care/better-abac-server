package care.better.abac.plugin.shedlock;

import care.better.abac.plugin.PluginManager.Key;
import care.better.abac.plugin.SynchronizationPhase;
import care.better.core.Opt;
import lombok.NonNull;
import net.javacrumbs.shedlock.core.DefaultLockManager;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockManager;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.time.Duration;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author Andrej Dolenc
 */
@EntityScan(basePackageClasses = Shedlock.class)
@EnableScheduling
@Configuration
public class ShedlockConfiguration implements BeanDefinitionRegistryPostProcessor {

    @Bean
    public LockProvider lockProvider(@NonNull DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }

    @Bean
    public LockManager lockManager(@NonNull LockProvider lockProvider) {
        return new DefaultLockManager(lockProvider,
                                      r -> Opt.of(r)
                                              .toType(RunnableWithLockConfiguration.class)
                                              .map(RunnableWithLockConfiguration::getLockConfiguration)
                                              .toOptional());
    }

    // Create LockableTaskScheduler dynamically after scheduling auto configuration of TaskScheduler is done from @EnableScheduling.
    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        GenericBeanDefinition myBeanDefinition = new GenericBeanDefinition();
        myBeanDefinition.setBeanClass(LockableTaskScheduler.class);
        myBeanDefinition.setScope(SCOPE_SINGLETON);
        registry.registerBeanDefinition("lockableTaskScheduler", myBeanDefinition);
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public static LockConfiguration create(
            @NonNull Key key,
            @NonNull SynchronizationPhase phase,
            @NonNull Duration lockAtMostFor,
            @NonNull Duration lockAtLeastFor) {
        String identifier = key.getId() + '-' + phase;
        String id = identifier.length() <= 255 ? identifier : Integer.toString(identifier.hashCode());
        return new LockConfiguration(id, lockAtMostFor, lockAtLeastFor);
    }
}
