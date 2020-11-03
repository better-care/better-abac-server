package care.better.abac.plugin;

import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.spi.PartyRelationService;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Andrej Dolenc
 */
public class PartyRelationServiceInitializer {

    private final PartyTypeRepository partyTypeRepository;
    private final RelationTypeRepository relationTypeRepository;

    public PartyRelationServiceInitializer(
            @NonNull PartyTypeRepository partyTypeRepository,
            @NonNull RelationTypeRepository relationTypeRepository) {
        this.partyTypeRepository = partyTypeRepository;
        this.relationTypeRepository = relationTypeRepository;
    }

    @Transactional
    public void initialize(PartyRelationService service) {
        service.providesFor().forEach(this::createRelationType);
    }

    private PartyType findOrCreatePartyType(String partyType) {
        return Optional.ofNullable(partyTypeRepository.findByName(partyType)).orElseGet(() -> {
            PartyType entity = new PartyType();
            entity.setName(partyType);
            return partyTypeRepository.save(entity);
        });
    }

    private void createRelationType(RelationType relationType) {
        Optional.ofNullable(relationTypeRepository.findByName(relationType.getName())).orElseGet(() -> {
            care.better.abac.jpa.entity.RelationType entity = new care.better.abac.jpa.entity.RelationType();
            entity.setName(relationType.getName());
            entity.setAllowedSource(findOrCreatePartyType(relationType.getSourcePartyType()));
            entity.setAllowedTarget(findOrCreatePartyType(relationType.getTargetPartyType()));
            return relationTypeRepository.save(entity);
        });
    }
}
