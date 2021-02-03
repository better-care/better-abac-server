package care.better.abac.plugin;

import care.better.abac.exception.PartyRelationInvalidTypesException;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.spi.PartyRelationService;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
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
        service.providesFor().forEach(this::findOrCreateRelationType);
    }

    private PartyType findOrCreatePartyType(String partyType) {
        return Optional.ofNullable(partyTypeRepository.findByName(partyType)).orElseGet(() -> {
            PartyType entity = new PartyType();
            entity.setName(partyType);
            return partyTypeRepository.save(entity);
        });
    }

    public care.better.abac.jpa.entity.RelationType findOrCreateRelationType(RelationType relationType) {
        care.better.abac.jpa.entity.RelationType entity =
                Optional.ofNullable(relationTypeRepository.findByName(relationType.getName())).orElseGet(() -> {
                    care.better.abac.jpa.entity.RelationType newEntity = new care.better.abac.jpa.entity.RelationType();
                    newEntity.setName(relationType.getName());
                    newEntity.setAllowedSource(findOrCreatePartyType(relationType.getSourcePartyType()));
                    newEntity.setAllowedTarget(findOrCreatePartyType(relationType.getTargetPartyType()));
                    return relationTypeRepository.save(newEntity);
                });
        if (!Objects.equals(relationType.getSourcePartyType(), entity.getAllowedSource().getName())
                || !Objects.equals(relationType.getTargetPartyType(), entity.getAllowedTarget().getName())) {
            throw new PartyRelationInvalidTypesException(String.format(
                    "Relation type %s already exists, but between different party types (%s->%s) than declared by the synchronization plugin (%s->%s)",
                    relationType.getName(),
                    entity.getAllowedSource().getName(),
                    entity.getAllowedTarget().getName(),
                    relationType.getSourcePartyType(),
                    relationType.getTargetPartyType()));
        }
        return entity;
    }
}
