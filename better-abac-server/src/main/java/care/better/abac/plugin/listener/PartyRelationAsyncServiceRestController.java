package care.better.abac.plugin.listener;

import care.better.abac.plugin.PluginManager;
import com.google.common.base.Preconditions;
import com.marand.core.Opt;
import care.better.abac.plugin.EndpointType;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static care.better.abac.plugin.listener.PartyRelationAsyncServiceRestController.STATIC_PATH;

/**
 * @author Andrej Dolenc
 */
@RestController
@RequestMapping(value = STATIC_PATH, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
public class PartyRelationAsyncServiceRestController {
    static final String STATIC_PATH = "/event/listener/callbacks";

    private final Map<String, ? extends AsyncPartyRelationService<?>> services;

    public PartyRelationAsyncServiceRestController(
            @Value("${async.rest.callback.port:}") String callbackPort,
            @Value("${spring.application.name:}") String applicationName,
            @Value("${server.port:}") String serverPort,
            @Value("${async.rest.base-callback-url:#{null}}") String baseCallbackUrl,
            @NonNull PluginManager pluginManager) throws UnknownHostException {
        String baseUrl;
        if (StringUtils.isNotBlank(baseCallbackUrl)) {
            baseUrl = getBaseUrl(baseCallbackUrl);
        } else {
            String implicitBaseUrl = getBaseUrl(serverPort, null);
            if (StringUtils.isBlank(implicitBaseUrl)) {
                Preconditions.checkArgument(StringUtils.isNotBlank(callbackPort), "async.rest.callback.port property required!");
                Preconditions.checkArgument(StringUtils.isNotBlank(applicationName), "spring.application.name property required!");
            }
            String explicitBaseUrl = getBaseUrl(callbackPort, applicationName);
            if (StringUtils.isNotBlank(implicitBaseUrl) && StringUtils.isNotBlank(explicitBaseUrl) && !StringUtils.equals(implicitBaseUrl, explicitBaseUrl)) {
                throw new IllegalArgumentException(String.format(
                        "Ambiguous callback URLs defined:\n %s \n %s \n" +
                                "Check the following properties: server.port %s " +
                                "spring.application.name %s " +
                                "and async.rest.callback.port %s", implicitBaseUrl, explicitBaseUrl, serverPort, applicationName, callbackPort));
            } else {
                baseUrl = StringUtils.isNotBlank(implicitBaseUrl) ? implicitBaseUrl : explicitBaseUrl;
            }
        }
        services = pluginManager.getServicesOfType(AsyncPartyRelationService.class).values().stream()
                .filter(service -> service.getEndpointType() == EndpointType.REST)
                .map(service -> Pair.of(UriUtils.encodePath(service.getId(), "UTF-8"), service))
                .peek(p -> p.getValue().configureListenerEndpoint(StringUtils.appendIfMissing(baseUrl, "/") + p.getKey()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private String getBaseUrl(String port, String applicationName) throws UnknownHostException {
        if (StringUtils.isBlank(port) || StringUtils.equals("0", port)) {
            return "";
        }
        String infixUrl = StringUtils.isNotBlank(applicationName) ? ('/' + applicationName) : "";
        return getBaseUrl("http://" + InetAddress.getLocalHost().getHostAddress() + ':' + port + infixUrl);
    }

    private String getBaseUrl(String externalApplicationUrl) {
        return StringUtils.appendIfMissing("/", externalApplicationUrl) + STATIC_PATH;
    }

    @RequestMapping(value = "/{serviceId}/{listenerId}", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eventCallback(
            @PathVariable("serviceId") String serviceId,
            @PathVariable("listenerId") String listenerId,
            @RequestBody Object eventData) {
        Opt.of(services.get(serviceId))
                .flatMap(service -> Opt.from(service.getListeners().stream()
                                                     .filter(listener -> listener.getId().equals(listenerId))
                                                     .findFirst()).ifPresent(listener -> listener.processEvent(eventData)));
    }
}
