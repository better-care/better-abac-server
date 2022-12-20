package care.better.abac.plugin.sync;

import care.better.abac.dto.PartyRelationDto;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.plugin.ChangeType;
import care.better.abac.plugin.PartyChangeMapper;
import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.PartyRelationServiceInitializer;
import care.better.abac.plugin.PartyRelationSynchronizer;
import care.better.abac.plugin.spi.SynchronizingPartyRelationService;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

/**
 * @author Andrej Dolenc
 */
public class PartyRelationSynchronizerTest {
    private static final Instant NOW = Instant.now();
    private static final care.better.abac.plugin.RelationType RELATION_TYPE =
            new care.better.abac.plugin.RelationType("PERSONAL_PHYSICIAN", "USER", "PATIENT");
    private static final care.better.abac.plugin.RelationType RELATION_TYPE_2 =
            new care.better.abac.plugin.RelationType("PERSONAL_DENTIST", "USER", "PATIENT");
    private static final care.better.abac.plugin.RelationType RELATION_TYPE_3 =
            new care.better.abac.plugin.RelationType("RELATED_TO", "USER", "PATIENT");
    private static final PartyType USER_TYPE = createType("USER");
    private static final PartyType PATIENT_TYPE = createType("PATIENT");
    private static final Party USER = createParty(1L, USER_TYPE);
    private static final Party PATIENT = createParty(2L, PATIENT_TYPE);
    private final PartyRelationRepository partyRelationRepository = Mockito.mock(PartyRelationRepository.class);
    private final PartyRepository partyRepository = Mockito.mock(PartyRepository.class);
    private final PartyTypeRepository partyTypeRepository = Mockito.mock(PartyTypeRepository.class);
    private final RelationTypeRepository relationTypeRepository = Mockito.mock(RelationTypeRepository.class);
    private final PartyChangeMapper partyChangeMapper = Mockito.mock(PartyChangeMapper.class);
    private final SynchronizingPartyRelationService service = Mockito.mock(SynchronizingPartyRelationService.class);
    private final PartyRelationServiceInitializer partyRelationServiceInitializer = new PartyRelationServiceInitializer(partyTypeRepository,
                                                                                                                        relationTypeRepository);
    private final PartyRelationSynchronizer synchronizer = new PartyRelationSynchronizer(partyRelationServiceInitializer,
                                                                                         partyRelationRepository,
                                                                                         partyRepository,
                                                                                         relationTypeRepository,
                                                                                         partyChangeMapper);

    @BeforeEach
    public void setUp() {
        Mockito.reset(partyRelationRepository, partyRepository, relationTypeRepository, partyChangeMapper, service);
        Mockito.when(partyChangeMapper.map(any()))
                .then(invocation -> new PartyRelationDto(1L, 1L, invocation.<PartyRelationChange>getArgument(0).getRelationType().getName(), 2L));
        Mockito.doReturn(Optional.of(USER)).when(partyRepository).findById(1L);
        Mockito.doReturn(Optional.of(PATIENT)).when(partyRepository).findById(2L);
        Mockito.doReturn(createRelationType("PERSONAL_PHYSICIAN", USER_TYPE, PATIENT_TYPE)).when(relationTypeRepository).findByName("PERSONAL_PHYSICIAN");
        Mockito.when(relationTypeRepository.save(any())).then(invocation -> invocation.getArgument(0));
        Mockito.when(partyTypeRepository.save(any())).then(invocation -> invocation.getArgument(0));
        Mockito.when(partyTypeRepository.findByName(any())).then(invocation -> {
            String typeName = invocation.getArgument(0);
            if ("USER".equals(typeName)) {
                return USER_TYPE;
            } else if ("PATIENT".equals(typeName)) {
                return PATIENT_TYPE;
            } else {
                return null;
            }
        });
        Mockito.doReturn(Collections.singleton(RELATION_TYPE)).when(service).providesFor();
    }

    @AfterEach
    public void tearDown() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void testSyncInitial() {
        PartyRelationChange change = new PartyRelationChange(Collections.singleton("1"), Collections.singleton("2"),
                                                             RELATION_TYPE, ChangeType.INSERT, null);
        Mockito.doReturn(Collections.singleton(change), Collections.emptySet()).when(service).syncInitial(eq(NOW));
        synchronizer.syncInitial(service, NOW);
        Mockito.verify(partyRelationRepository).deleteByPartyAndRelationType(isNull(), isNull(), eq("PERSONAL_PHYSICIAN"));
        Mockito.verify(partyRelationRepository).save(argThat(
                rel -> USER.equals(rel.getSource()) && PATIENT.equals(rel.getTarget()) && "PERSONAL_PHYSICIAN".equals(rel.getRelationType().getName())));
    }

    @Test
    public void testSync() {
        PartyRelationChange delete = new PartyRelationChange(Collections.singleton("1"), Collections.singleton("2"),
                                                             RELATION_TYPE, ChangeType.DELETE, null);
        PartyRelationChange insert = new PartyRelationChange(Collections.singleton("1"), Collections.singleton("2"),
                                                             RELATION_TYPE, ChangeType.INSERT, null);

        Mockito.doReturn(Sets.newHashSet(delete, insert)).when(service).sync(eq(NOW), eq(NOW.plusSeconds(1)));
        synchronizer.sync(service, NOW, NOW.plusSeconds(1));
        Mockito.verify(partyRelationRepository).deleteByPartyAndRelationType(eq("1"), eq("2"), eq("PERSONAL_PHYSICIAN"));
        Mockito.verify(partyRelationRepository).save(argThat(
                rel -> USER.equals(rel.getSource()) && PATIENT.equals(rel.getTarget()) && "PERSONAL_PHYSICIAN".equals(rel.getRelationType().getName())));

    }

    @Test
    public void testSyncNewRelation() {
        PartyRelationChange delete = new PartyRelationChange(Collections.singleton("1"), Collections.singleton("2"),
                                                             RELATION_TYPE_2, ChangeType.DELETE, null);
        PartyRelationChange insert = new PartyRelationChange(Collections.singleton("1"), Collections.singleton("2"),
                                                             RELATION_TYPE_3, ChangeType.INSERT, null);

        Mockito.doReturn(Sets.newHashSet(delete, insert)).when(service).sync(eq(NOW), eq(NOW.plusSeconds(1)));
        synchronizer.sync(service, NOW, NOW.plusSeconds(1L));
        Mockito.verify(relationTypeRepository).save(argThat(
                rt -> RELATION_TYPE_3.getName().equals(rt.getName())
                        && "USER".equals(rt.getAllowedSource().getName())
                        && "PATIENT".equals(rt.getAllowedTarget().getName())));
        Mockito.verify(partyRelationRepository).deleteByPartyAndRelationType(eq("1"), eq("2"), eq("PERSONAL_DENTIST"));
        Mockito.verify(partyRelationRepository).save(argThat(
                rel -> USER.equals(rel.getSource()) && PATIENT.equals(rel.getTarget()) && "RELATED_TO".equals(rel.getRelationType().getName())));

    }

    private RelationType createRelationType(String name, PartyType source, PartyType target) {
        RelationType relationType = new RelationType();
        relationType.setName(name);
        relationType.setAllowedSource(source);
        relationType.setAllowedTarget(target);
        return relationType;
    }

    private static Party createParty(Long id, PartyType type) {
        Party party = new Party(id);
        party.setType(type);
        party.getExternalIds().add(Long.toString(id));
        return party;
    }

    private static PartyType createType(String name) {
        PartyType type = new PartyType();
        type.setName(name);
        return type;
    }
}
