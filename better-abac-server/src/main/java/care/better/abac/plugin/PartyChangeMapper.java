package care.better.abac.plugin;

import care.better.abac.dto.PartyRelationDto;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.rest.MappingUtils;
import care.better.core.Opt;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public class PartyChangeMapper {

    private final PartyRepository partyRepository;
    private final PartyTypeRepository partyTypeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PartyChangeMapper(@NonNull PartyRepository partyRepository, @NonNull PartyTypeRepository partyTypeRepository) {
        this.partyRepository = partyRepository;
        this.partyTypeRepository = partyTypeRepository;
    }

    @Transactional
    public PartyRelationDto map(@NonNull PartyRelationChange change) {
        PartyRelationDto dto = new PartyRelationDto();
        dto.setValidUntil(change.getValidUntil());
        dto.setRelationType(change.getRelationType().getName());
        dto.setSource(updateOrCreateParty(change.getRelationType().getSourcePartyType(), change.getSourceExternalId()).getId());
        dto.setTarget(updateOrCreateParty(change.getRelationType().getTargetPartyType(), change.getTargetExternalId()).getId());
        return dto;
    }

    private Party updateOrCreateParty(String type, Set<String> externalIds) {
        Party party = Optional.ofNullable(partyRepository.findByTypeAndExternalId(type, externalIds)).orElseGet(() -> {
            Party newParty = new Party();
            PartyType partyType = Opt.of(partyTypeRepository.findByName(type))
                    .orElseGet(() -> {
                        PartyType entity = new PartyType();
                        entity.setName(type);
                        partyTypeRepository.save(entity);
                        return entity;
                    });
            newParty.setType(partyType);
            return newParty;
        });
        party.getExternalIds().addAll(externalIds);
        partyRepository.save(party);
        entityManager.flush();
        return party;
    }
}
