package care.better.abac.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import care.better.abac.dto.config.AbstractExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemEventType;
import care.better.abac.dto.config.ValidationErrorDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Matic Ribič
 */
public class ExternalSystemRestClient {
    private static final Logger log = LogManager.getLogger(ExternalSystemRestClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalSystemRestClient(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(20000);
        httpRequestFactory.setReadTimeout(20000);
        restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.setErrorHandler(new BasicResponseErrorHandler());

        this.objectMapper = objectMapper;
    }

    public boolean notify(String serverBaseRestUrl, ExternalSystemEventType eventType) {
        Objects.requireNonNull(eventType, "Client event type");
        try {
            ResponseEntity<Void> response = restTemplate.exchange(getServerBaseRestUrl(serverBaseRestUrl) + "/notify?eventType={eventType}",
                                                                  HttpMethod.POST, null, Void.class, eventType);

            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            } else {
                log.warn("POST request '{}/notify?eventType={}' has invalid response status code {} (200 is expected).",
                         serverBaseRestUrl,
                         eventType,
                         response.getStatusCode());
                return false;
            }
        } catch (RestClientException e) {
            log.warn("Notification failed with error: {}", e.getMessage(), e);
            return false;
        }
    }

    public void validateConfiguration(AbstractExternalSystemDto clientConfigInputDto) throws ValidationException {
        Objects.requireNonNull(clientConfigInputDto, "Client config");
        Objects.requireNonNull(clientConfigInputDto.getAbacRestBaseUrl(), "System ABAC REST base URL");
        String serverBaseRestUrl = getServerBaseRestUrl(clientConfigInputDto.getAbacRestBaseUrl());

        try {
            ResponseEntity<String> response = restTemplate.exchange(serverBaseRestUrl + "/config/validate",
                                                                    HttpMethod.POST,
                                                                    new HttpEntity<>(clientConfigInputDto, createHeaders()),
                                                                    String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw ValidationException.of(response.getBody() != null ? readErrors(response.getBody()) : Collections.emptyList());
            }
        } catch (RestClientException e) {
            log.warn("Validation failed with error: {}", e.getMessage(), e);
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw ValidationException.of(Collections.singletonList(new ValidationErrorDto(null, e.getMessage())));
        }
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    private List<ValidationErrorDto> readErrors(String errorData) {
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, ValidationErrorDto.class);
        try {
            return objectMapper.readValue(errorData, type);
        } catch (JsonProcessingException e) {
            return Collections.singletonList(new ValidationErrorDto(null, errorData));
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getServerBaseRestUrl(String serverBaseRestUrl) {
        return serverBaseRestUrl.endsWith("/") ? serverBaseRestUrl.substring(0, serverBaseRestUrl.length() - 1) : serverBaseRestUrl;
    }

    private static class BasicResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
        }

        @Override
        public void handleError(ClientHttpResponse response) {
        }
    }
}
