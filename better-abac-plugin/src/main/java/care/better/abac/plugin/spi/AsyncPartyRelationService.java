package care.better.abac.plugin.spi;

import care.better.abac.plugin.EndpointType;
import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.listener.Listener;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public interface AsyncPartyRelationService<E extends Listener> extends PartyRelationService {
    Set<PartyRelationChange> syncInitial();

    Collection<E> getListeners();

    void configureListenerEndpoint(String endpoint);

    EndpointType getEndpointType();
}
