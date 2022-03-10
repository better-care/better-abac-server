package care.better.abac.rest;

import care.better.abac.dto.ApplicationInfoDto;
import care.better.abac.version.VersionProvider;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author Gregor Berger
 */
@RestController
@RequestMapping("/rest/v1/app")
public class AppRestController {
    private final String TIMEZONE = ZoneId.systemDefault().getId();
    private final int OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds();
    private final VersionProvider versionProvider;

    public AppRestController(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationInfoDto> getApplicationMetaData() {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .varyBy("Authorization")
                    .body(new ApplicationInfoDto(versionProvider.getVersion(), TIMEZONE, OFFSET));
    }
}
