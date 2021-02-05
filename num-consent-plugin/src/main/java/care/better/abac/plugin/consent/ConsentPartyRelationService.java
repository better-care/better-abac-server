package care.better.abac.plugin.consent;

import care.better.abac.plugin.EndpointType;
import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.RelationType;
import care.better.abac.plugin.auth.AuthorizationProvider;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import lombok.NonNull;

/** @author Alex Karle */
public class ConsentPartyRelationService
    implements AsyncPartyRelationService<ConsentEventListener> {

  private List<ConsentEventListener> listeners;

  @Override
  public String getId(){
    return "consent";
  }

  @Override
  public Set<PartyRelationChange> syncInitial() {
    return Collections.emptySet();
  }

  @Override
  public Collection<ConsentEventListener> getListeners() {
    return List.copyOf(listeners);
  }

  @Override
  public Set<RelationType> providesFor() {
    return Collections.emptySet();
  }

  @Override
  public void configure(Properties properties, AuthorizationProvider authorizationProvider) {
    listeners = List.of(new ConsentEventListener());
  }

  @Override
  public void configureListenerEndpoint(@NonNull String endpoint) {
    //NOOP
  }

  @Override
  public EndpointType getEndpointType() {
    return EndpointType.REST;
  }
}
