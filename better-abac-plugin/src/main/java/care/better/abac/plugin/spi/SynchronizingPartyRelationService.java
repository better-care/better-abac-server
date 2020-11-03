package care.better.abac.plugin.spi;

import care.better.abac.plugin.PartyRelationChange;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author Andrej Dolenc
 */
public interface SynchronizingPartyRelationService extends PartyRelationService {
    Set<PartyRelationChange> syncInitial(Instant now);

    Set<PartyRelationChange> sync(Instant from, Instant to);
}
