package care.better.abac.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public abstract class AbstractPartyInfoService implements PartyInfoService {

    private final Logger log = LogManager.getLogger(AbstractPartyInfoService.class);

    @Override
    public String getFullName(Set<String> externalIds) {
        return externalIds.stream()
                .map(this::resolveExternalId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> resolveIfMissing(externalIds));
    }

    public abstract String resolveExternalId(String externalId);

    protected String resolveIfMissing(Set<String> externalIds) {
        log.warn("Result not found for patient ids {}!", String.join(",", externalIds));
        return null;
    }
}
