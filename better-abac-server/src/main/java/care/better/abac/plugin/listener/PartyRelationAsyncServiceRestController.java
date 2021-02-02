package care.better.abac.plugin.listener;

import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.PartyRelationSynchronizer;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import care.better.core.Opt;
import care.better.core.function.FunctionWithThrowable;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static care.better.abac.plugin.listener.PartyRelationAsyncServiceRestController.STATIC_PATH;

/**
 * @author Andrej Dolenc
 */
@RestController
@RequestMapping(value = STATIC_PATH, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
public class PartyRelationAsyncServiceRestController {
    public static final String STATIC_PATH = "/rest/v1/event/listener/callbacks";

    private final PartyRelationSynchronizer partyRelationSynchronizer;
    private final Map<String, AsyncPartyRelationService<?>> services = new ConcurrentHashMap<>();

    public PartyRelationAsyncServiceRestController(@NonNull PartyRelationSynchronizer partyRelationSynchronizer) {
        this.partyRelationSynchronizer = partyRelationSynchronizer;
    }

    public void addListenerServices(Map<String, AsyncPartyRelationService<?>> newServices) {
        services.putAll(newServices);
    }

    @RequestMapping(value = "/{serviceId}/{listenerId}", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eventCallback(
            @PathVariable("serviceId") String serviceId,
            @PathVariable("listenerId") String listenerId,
            @RequestBody Object eventData) {
        Opt.resolve(() -> services.get(serviceId))
                .flatMap((FunctionWithThrowable<AsyncPartyRelationService<?>, Opt<Set<PartyRelationChange>>, RuntimeException>)service ->
                        Opt.from(service.getListeners().stream().filter(listener -> listener.getId().equals(listenerId)).findFirst())
                                .map(listener -> listener.processEvent(eventData)))
                .ifPresent(partyRelationSynchronizer::sync)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No service configured for listener %s and service %s , rejecting message!", listenerId, serviceId)));
    }
}
