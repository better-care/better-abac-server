package care.better.abac.plugin.listener;

import care.better.abac.plugin.PartyRelationChange;

import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public interface Listener {
    String getId();

    Set<PartyRelationChange> processEvent(Object event);
}
