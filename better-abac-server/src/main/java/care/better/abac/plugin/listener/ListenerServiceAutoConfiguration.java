package care.better.abac.plugin.listener;

import care.better.abac.plugin.condition.ConditionalOnServiceType;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Andrej Dolenc
 */
@Configuration
@ConditionalOnServiceType(AsyncPartyRelationService.class)
@Import(PartyRelationAsyncServiceRestController.class)
@AutoConfigureAfter(PluginConfiguration.class)
public class ListenerServiceAutoConfiguration {
}
