package care.better.abac.plugin;

import care.better.abac.dto.PartyRelationDto;
import care.better.abac.exception.PartyRelationInvalidTypesException;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.spi.AsyncPartyRelationService;
import care.better.abac.plugin.spi.SynchronizingPartyRelationService;
import care.better.core.Opt;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andrej Dolenc
 */
public class PartyRelationSynchronizer {
    private static final Logger log = LogManager.getLogger(PartyRelationSynchronizer.class);

    private final PartyRelationServiceInitializer partyRelationServiceInitializer;
    private final PartyRelationRepository partyRelationRepository;
    private final PartyRepository partyRepository;
    private final RelationTypeRepository relationTypeRepository;
    private final PartyChangeMapper partyChangeMapper;

    public PartyRelationSynchronizer(
            @NonNull PartyRelationServiceInitializer partyRelationServiceInitializer,
            @NonNull PartyRelationRepository partyRelationRepository,
            @NonNull PartyRepository partyRepository,
            @NonNull RelationTypeRepository relationTypeRepository,
            @NonNull PartyChangeMapper partyChangeMapper) {
        this.partyRelationServiceInitializer = partyRelationServiceInitializer;
        this.partyRelationRepository = partyRelationRepository;
        this.partyRepository = partyRepository;
        this.relationTypeRepository = relationTypeRepository;
        this.partyChangeMapper = partyChangeMapper;
    }

    @Transactional
    public void syncInitial(@NonNull SynchronizingPartyRelationService service, @NonNull Instant now) {
        service.providesFor()
                .forEach(rt -> partyRelationRepository.deleteByPartyAndRelationType(null, null, rt.getName()));
        service.syncInitial(now).stream()
                .filter(r -> ChangeType.INSERT == r.getChangeType())
                .forEach(c -> partyRelationRepository.save(create(c)));
    }

    @Transactional
    public void syncInitial(@NonNull AsyncPartyRelationService<?> service) {
        service.providesFor()
                .forEach(rt -> partyRelationRepository.deleteByPartyAndRelationType(null, null, rt.getName()));
        service.syncInitial().stream()
                .filter(r -> ChangeType.INSERT == r.getChangeType())
                .forEach(c -> partyRelationRepository.save(create(c)));
    }

    @Transactional
    public void sync(
            @NonNull SynchronizingPartyRelationService service,
            @NonNull Instant from,
            @NonNull Instant to) {
        Set<PartyRelationChange> relations = service.sync(from, to);
        log.debug("Got changes {}", relations);
        sync(relations);
    }

    @Transactional
    public void sync(Set<PartyRelationChange> relations) {
        relations.stream()
                .filter(r -> ChangeType.DELETE == r.getChangeType())
                .forEach(r -> partyRelationRepository.deleteByPartyAndRelationType(
                        getFirstExternalId(r.getSourceExternalId()),
                        getFirstExternalId(r.getTargetExternalId()),
                        r.getRelationType().getName()));
        relations.stream()
                .filter(r -> ChangeType.INSERT == r.getChangeType())
                .forEach(c -> partyRelationRepository.save(create(c)));
    }

    private String getFirstExternalId(Set<String> externalIds) {
        return Optional.ofNullable(externalIds).flatMap(s -> s.stream().findFirst()).orElse(null);
    }

    private PartyRelation create(PartyRelationChange change) {
        PartyRelationDto dto = partyChangeMapper.map(change);
        PartyRelation partyRelation = new PartyRelation();
        map(dto, partyRelation);
        validate(partyRelation);
        return partyRelation;
    }

    private void map(PartyRelationDto dto, PartyRelation partyRelation) {
        partyRelation.setSource(partyRepository.findById(dto.getSource()).get());
        partyRelation.setTarget(partyRepository.findById(dto.getTarget()).get());
        RelationType relationType = Opt.of(relationTypeRepository.findByName(dto.getRelationType()))
                .orElseGet(() -> partyRelationServiceInitializer.findOrCreateRelationType(new care.better.abac.plugin.RelationType(
                        dto.getRelationType(), partyRelation.getSource().getType().getName(), partyRelation.getTarget().getType().getName()
                )));
        partyRelation.setRelationType(relationType);
        partyRelation.setValidUntil(dto.getValidUntil());
    }

    private void validate(PartyRelation partyRelation) {
        PartyType allowedSource = partyRelation.getRelationType().getAllowedSource();
        if (allowedSource != null && !allowedSource.equals(partyRelation.getSource().getType())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid source party type: " + partyRelation.getSource()
                            .getType()
                            .getName() + ", allowed type: " + allowedSource.getName() + '!');
        }
        PartyType allowedTarget = partyRelation.getRelationType().getAllowedTarget();
        if (allowedTarget != null && !allowedTarget.equals(partyRelation.getTarget().getType())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid target party type: " + partyRelation.getTarget()
                            .getType()
                            .getName() + ", allowed type: " + allowedTarget.getName() + '!');
        }
    }
}

