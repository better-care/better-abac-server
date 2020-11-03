package care.better.abac.plugin.spi;

import care.better.abac.plugin.RelationType;

import java.util.Properties;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public interface PartyRelationService extends Service {

    Set<RelationType> providesFor();
}
