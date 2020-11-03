package care.better.abac.rest;

import care.better.abac.dto.config.ExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemInputDto;
import care.better.abac.external.ExternalSystemService;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.rest.client.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author Matic Ribic
 */
@Component
@RestController
@RequestMapping(ExternalSystemResource.BASE_PATH)
public class ExternalSystemResource {
    private final ExternalSystemService externalSystemService;

    public static final String BASE_PATH = "/rest/v1/admin/client/config";

    @Autowired
    public ExternalSystemResource(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }

    @GetMapping(value = "/{systemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExternalSystemDto> getConfig(
            @PathVariable("systemId") String systemId,
            @RequestParam(value = "configHash", required = false) String configHash) {
        ExternalSystemDto configDto = externalSystemService.getConfigDto(systemId);
        if (configDto == null) {
            return ResponseEntity.notFound().build();
        } else if (StringUtils.isNotBlank(configHash) && configHash.equals(configDto.getConfigHash())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        } else {
            return ResponseEntity.ok(configDto);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createConfig(
            @RequestParam(value = "validate", defaultValue = "true") boolean validate,
            @RequestBody ExternalSystemInputDto inputDto) throws ValidationException {
        ExternalSystemEntity entity = externalSystemService.createConfigAndNotify(inputDto, validate);

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentServletMapping()
                                              .path(BASE_PATH + "/" + entity.getExternalId())
                                              .build()
                                              .toUri()).build();
    }

    @PutMapping(value = "/{systemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateConfig(
            @PathVariable("systemId") String systemId,
            @RequestParam(value = "validate", defaultValue = "true") boolean validate,
            @RequestBody ExternalSystemInputDto inputDto) throws ValidationException {
        ExternalSystemEntity entity = externalSystemService.updateConfigAndNotify(systemId, inputDto, validate);
        return entity != null ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
