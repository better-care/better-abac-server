package care.better.abac.content;

import com.google.common.collect.ImmutableSet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

/**
 * @author Matic Ribič
 */
@Configuration
@ConfigurationProperties(prefix = "content-sync")
public class AppContentSyncConfiguration {
    private PartyTypes partyTypes;
    private RelationTypes relationTypes;

    public PartyTypes getPartyTypes() {
        return partyTypes != null ? partyTypes : PartyTypes.EMPTY;
    }

    public void setPartyTypes(PartyTypes partyTypes) {
        this.partyTypes = partyTypes;
    }

    public RelationTypes getRelationTypes() {
        return relationTypes != null ? relationTypes : RelationTypes.EMPTY;
    }

    public void setRelationTypes(RelationTypes relationTypes) {
        this.relationTypes = relationTypes;
    }

    public static class PartyTypes {
        private Set<String> included = Collections.emptySet();

        private static final PartyTypes EMPTY = new PartyTypes(Collections.emptySet());

        public PartyTypes() {
        }

        private PartyTypes(Set<String> included) {
            this.included = ImmutableSet.copyOf(included);
        }

        public Set<String> getIncluded() {
            return included;
        }

        public void setIncluded(Set<String> included) {
            this.included = included != null ? ImmutableSet.copyOf(included) : Collections.emptySet();
        }
    }

    public static final class RelationTypes {
        private Set<String> included = Collections.emptySet();

        private static final RelationTypes EMPTY = new RelationTypes(Collections.emptySet());

        public RelationTypes() {
        }

        private RelationTypes(Set<String> included) {
            this.included = ImmutableSet.copyOf(included);
        }

        public Set<String> getIncluded() {
            return included;
        }

        public void setIncluded(Set<String> included) {
            this.included = included != null ? ImmutableSet.copyOf(included) : Collections.emptySet();
        }
    }
}
