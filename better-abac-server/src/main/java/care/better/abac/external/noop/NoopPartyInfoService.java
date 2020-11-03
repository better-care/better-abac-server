package care.better.abac.external.noop;

import care.better.abac.external.PartyInfoService;

import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class NoopPartyInfoService implements PartyInfoService {
    @Override
    public String getFullName(Set<String> externalIds) {
        return String.join(";", externalIds);
    }
}
