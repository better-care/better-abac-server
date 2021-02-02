package care.better.abac.external.demographics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.collect.Lists;
import care.better.core.Opt;
import care.better.abac.external.AbstractRestPartyInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Bostjan Lah
 */
public class DemographicsPartyInfoService extends AbstractRestPartyInfoService {
    private static final Logger log = LogManager.getLogger(DemographicsPartyInfoService.class.getName());
    private static final Pattern NUMERIC = Pattern.compile("[0-9]+");

    private final String baseRestUrl;
    private final String resourceName;
    private final String identifier;

    public DemographicsPartyInfoService(String baseRestUrl, String resourceName, String identifier) {
        this.baseRestUrl = baseRestUrl;
        this.resourceName = resourceName;
        this.identifier = identifier;

        List<HttpMessageConverter<?>> messageConverters = getRestTemplate().getMessageConverters();
        Opt<AbstractHttpMessageConverter<?>> jsonConverter = Opt.from(messageConverters.stream()
                .filter(c -> AbstractHttpMessageConverter.class.isAssignableFrom(c.getClass()))
                .filter(c -> c.getSupportedMediaTypes().contains(MediaType.APPLICATION_JSON)).findFirst())
                .toType(Object.class);
        jsonConverter.ifPresent(this::addFhirJsonMediaType);
    }

    @Override
    @Cacheable(value = "patients", keyGenerator = "setKeyGenerator", unless = "#result == null")
    public String getFullName(Set<String> externalIds) {
        return super.getFullName(externalIds);
    }

    @Override
    public String resolveExternalId(String externalId)
    {
        try {
            JsonNode node = getDemographicsResponse(externalId);
            if (!node.isMissingNode()) {
                JsonNode firstNames = node.path("family").path(0);
                JsonNode lastNames = node.path("given").path(0);
                return formatFullName(firstNames, lastNames);
            }
        } catch (RestClientException e) {
            log.warn("Unable to retrieve patient info {}!", e.getMessage());
        }
        return null;
    }

    private JsonNode getDemographicsResponse(String id) {
        ResponseEntity<JsonNode> responseEntity;
        if (NUMERIC.matcher(id).matches()) {
            // resource id
            responseEntity = getRestTemplate().exchange(
                    baseRestUrl + '/' + resourceName + "/{id}",
                    HttpMethod.GET,
                    getHttpEntity(),
                    JsonNode.class,
                    id);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody().path("name").path(0);
            }
        } else {
            // identifier
            responseEntity = getRestTemplate().exchange(
                    baseRestUrl + '/' + resourceName + "?identifier=" + identifier + '|' + id,
                    HttpMethod.GET,
                    getHttpEntity(),
                    JsonNode.class,
                    id);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody().path("entry").path(0).path("name").path(0);
            }
        }
        return MissingNode.getInstance();
    }

    private void addFhirJsonMediaType(AbstractHttpMessageConverter<?> converter) {
        List<MediaType> mediaTypes = Lists.newArrayList(converter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.asMediaType(MimeType.valueOf("application/json+fhir")));
        converter.setSupportedMediaTypes(mediaTypes);
    }
}
