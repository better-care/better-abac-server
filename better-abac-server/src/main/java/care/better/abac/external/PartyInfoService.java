package care.better.abac.external;

import java.util.Set;

/**
 * @author Bostjan Lah
 */
public interface PartyInfoService {
    String getFullName(Set<String> externalIds);
}
