package care.better.abac.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.RelationTypeDto;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.repo.PolicyRepository;
import care.better.abac.policy.execute.evaluation.BooleanEvaluationExpression;
import care.better.abac.policy.execute.evaluation.BooleanOperation;
import care.better.abac.policy.execute.evaluation.BooleanOperationEvaluationExpression;
import care.better.abac.policy.execute.evaluation.EvaluationExpression;
import care.better.abac.policy.execute.evaluation.ResultSetEvaluationExpression;
import care.better.abac.policy.execute.evaluation.Tag;
import care.better.abac.policy.execute.evaluation.TagSetEvaluationExpression;
import care.better.abac.rest.PolicyExecutionResourceTest.Config;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Bostjan Lah
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, Config.class})
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureTestDatabase
@TestPropertySource(properties = "sso.enabled = false")
public class PolicyExecutionResourceTest {
    private static final String BASE_URL = "/rest/v1";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PolicyRepository policyRepository;

    @Value("${local.server.port}")
    private int serverPort;

    @Test
    public void executeComplexPolicy() {
        String policyName = "COMPLEX_POLICY_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("ANY_OF(ALL_OF(hasAnyRelation(ctx.user, ctx.patient, 'OWNS', 'HAS'), hasTag('TAG')), NONE_OF(hasAnyTag('TAG2::value')))");
        policyRepository.save(policy);

        assertThat(HttpStatus.CONFLICT == executeSimple(policyName, ImmutableMap.of("user", "?", "patient", "patient1")));

        BooleanOperationEvaluationExpression expression = (BooleanOperationEvaluationExpression)executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient1"));
        assertThat(expression.getBooleanOperation()).isEqualTo(BooleanOperation.OR);
        BooleanOperationEvaluationExpression allOf = (BooleanOperationEvaluationExpression)expression.getLeftChild();
        BooleanOperationEvaluationExpression noneOf = (BooleanOperationEvaluationExpression)expression.getRightChild();
        assertThat(allOf.getBooleanOperation()).isEqualTo(BooleanOperation.AND);
        assertThat(toResultSet(allOf.getLeftChild())).isEqualTo(Sets.newHashSet("user", "user1", "user2"));
        assertThat(toTagSet(allOf.getRightChild())).isEqualTo(Collections.singleton(new Tag("TAG")));
        assertThat(noneOf.getBooleanOperation()).isEqualTo(BooleanOperation.NOT);
        assertThat(toTagSet(noneOf.getLeftChild())).isEqualTo(Collections.singleton(new Tag("TAG2::value")));
    }

    @Test
    public void executeHasAnyRelationPolicy() {
        String policyName = "HAS_ANY_RELATION_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasAnyRelation(ctx.user, ctx.patient, 'OWNS', 'HAS')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user", "patient", Lists.newArrayList("patient")))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient")))))
                .isEqualTo(Sets.newHashSet("user", "user1"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient1")))))
                .isEqualTo(Sets.newHashSet("user", "user1", "user2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient2")))))
                .isEqualTo(Sets.newHashSet("user", "user1"));

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?"))))
                .isEqualTo(Sets.newHashSet("patient", "patient1", "patient2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?"))))
                .isEqualTo(Sets.newHashSet("patient", "patient1", "patient2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?"))))
                .isEqualTo(Sets.newHashSet("patient1"));
    }

    @Test
    public void executeHasRelationPolicy() {
        String policyName = "HAS_RELATION_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasRelation(ctx.user, 'OWNS', ctx.patient)");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user", "patient", Lists.newArrayList("patient")))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient"))))).isEqualTo(Sets.newHashSet("user"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient1"))))).isEqualTo(Sets.newHashSet("user", "user1", "user2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", Lists.newArrayList("patient2"))))).isEqualTo(Sets.newHashSet("user", "user1"));

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?")))).isEqualTo(Sets.newHashSet("patient", "patient1", "patient2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?")))).isEqualTo(Sets.newHashSet("patient1", "patient2"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?")))).isEqualTo(Sets.newHashSet("patient1"));

    }

    @Test
    public void executeHasRelationPolicyWithFixedValue() {
        String policyName = "HAS_RELATION_TEST2";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasRelation(ctx.user, 'OWNS', 'patient1')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user"))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?")))).isEqualTo(Sets.newHashSet("user", "user1", "user2"));
    }

    @Test
    public void executeHasRelationChainPolicy() {
        String policyName = "HAS_RELATION_CHAIN_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasRelationChain(ctx.user, ctx.patient, 'MEMBER_OF', 'PROVIDES_FOR')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user1", "patient", "patient1"))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient3")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient")))).isEqualTo(Sets.newHashSet("user1", "user2"));

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?")))).isEqualTo(Sets.newHashSet("patient", "patient1"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?")))).isEqualTo(Sets.newHashSet("patient", "patient2"));
    }

    @Test
    public void executeHasPrefixRelationChainPolicy() {
        String policyName = "HAS_RELATION_CHAIN_PREFIX_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasRelationChain(ctx.user, ctx.patient, 'INCLUDED_IN', '>ASSIGNED_TO', '<CONSENTS_TO')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user", "patient", "patient"))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient2")))).isEqualTo(Sets.newHashSet("user1"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient3")))).isEqualTo(Sets.newHashSet("user"));

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?")))).isEqualTo(Sets.newHashSet("patient", "patient3"));
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?")))).isEqualTo(Sets.newHashSet("patient1", "patient2"));
    }

    @Test
    public void executeMatchRelationsPolicy() {
        String policyName = "MATCH_RELATIONS_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("matchRelations(ctx.user, 'USES', ctx.patient, 'USES')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user2", "patient", "patient2"))).isEqualTo(HttpStatus.OK);

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient1")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient2")))).isEqualTo(Sets.newHashSet("user2"));

        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?")))).isEmpty();
        assertThat(toResultSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?")))).isEqualTo(Sets.newHashSet("patient2"));
    }

    @Test
    public void executeMatchRelationsTagConversionPolicy() {
        String policyName = "MATCH_RELATIONS_CONVERSION_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("prefix(asTags(matchRelations(ctx.user, 'USES', ctx.patient, 'USES'),'CARE_PLAN'),'CarePlan/')");
        policyRepository.save(policy);

        assertThat(executeSimple(policyName, ImmutableMap.of("user", "user2", "patient", "patient2"))).isEqualTo(HttpStatus.OK);

        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient")))).isEmpty();
        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient1")))).isEmpty();
        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?", "patient", "patient2")))).isEqualTo(Sets.newHashSet(new Tag(
                "CARE_PLAN::CarePlan/user2")));

        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "user", "patient", "?")))).isEmpty();
        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "user1", "patient", "?")))).isEmpty();
        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "user2", "patient", "?")))).isEqualTo(Sets.newHashSet(new Tag(
                "CARE_PLAN::CarePlan/patient2")));
    }

    @Test
    public void executeHasTagPolicy() {
        String policyName = "HAS_TAG_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasTag('tag','value')");
        policyRepository.save(policy);

        EvaluationExpression response = executeComplex(policyName, ImmutableMap.of("user", "user"));
        assertThat(toTagSet(response)).isEqualTo(Collections.singleton(new Tag("tag::value")));

        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?")))).isEqualTo(Collections.singleton(new Tag("tag::value")));
    }

    @Test
    public void executeHasTagWithEmptyValuePolicy() {
        String policyName = "HAS_TAG_TEST2";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasTag('tag')");
        policyRepository.save(policy);

        EvaluationExpression response = executeComplex(policyName, ImmutableMap.of("user", "user"));
        assertThat(toTagSet(response)).isEqualTo(Collections.singleton(new Tag("tag")));

        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?")))).isEqualTo(Collections.singleton(new Tag("tag", null)));
    }

    @Test
    public void executeHasAnyTagPolicy() {
        String policyName = "HAS_ANY_TAG_TEST";
        Policy policy = new Policy();
        policy.setName(policyName);
        policy.setPolicy("hasAnyTag('tag','tag::value')");
        policyRepository.save(policy);

        EvaluationExpression response = executeComplex(policyName, ImmutableMap.of("user", "user"));
        assertThat(toTagSet(response)).isEqualTo(Sets.newHashSet(new Tag("tag", "value"), new Tag("tag")));

        assertThat(toTagSet(executeComplex(policyName, ImmutableMap.of("user", "?")))).isEqualTo(Sets.newHashSet(new Tag("tag", "value"), new Tag("tag")));
    }

    private HttpStatus executeSimple(String name, Map<String, Object> context) {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(BASE_URL + "/policy/execute/name/{name}", context, Void.class, name);
        return responseEntity.getStatusCode();
    }

    private EvaluationExpression executeComplex(String name, Map<String, Object> context) {
        ResponseEntity<EvaluationExpression> response = restTemplate.postForEntity(BASE_URL + "/policy/execute/name/{name}/expression", context, EvaluationExpression.class, name);
        assertThat(response.getStatusCode().is2xxSuccessful());
        return response.getBody();
    }

    private Set<String> toResultSet(EvaluationExpression expression) {
        if (expression instanceof BooleanEvaluationExpression) {
            boolean value = ((BooleanEvaluationExpression)expression).getBooleanValue();
            return value ? null : Collections.emptySet();
        }
        return ((ResultSetEvaluationExpression)expression).getExternalIds();
    }

    private Set<Tag> toTagSet(EvaluationExpression expression) {
        if (expression instanceof BooleanEvaluationExpression) {
            boolean value = ((BooleanEvaluationExpression)expression).getBooleanValue();
            return value ? null : Collections.emptySet();
        }
        return ((TagSetEvaluationExpression)expression).getTags();
    }

    @TestConfiguration
    public static class Config {

        @Autowired
        private TestRestTemplate restTemplate;

        @Bean
        public ApplicationRunner initializer() {
            return args -> {
                createPartyType("USER");
                createPartyType("PATIENT");
                createPartyType("ORGANIZATION");
                createPartyType("CARE_TEAM");
                createPartyType("STUDY");
                createRelationType("OWNS", "USER", "PATIENT");
                createRelationType("HAS", "USER", "PATIENT");
                createRelationType("USES", "USER", "PATIENT");
                createRelationType("MEMBER_OF", "USER", "ORGANIZATION");
                createRelationType("PROVIDES_FOR", "ORGANIZATION", "PATIENT");
                createRelationType("INCLUDED_IN", "USER", "CARE_TEAM");
                createRelationType("ASSIGNED_TO", "CARE_TEAM", "STUDY");
                createRelationType("CONSENTS_TO", "PATIENT", "STUDY");

                PartyDto user = createParty("USER", "user");
                PartyDto user1 = createParty("USER", "user1");
                PartyDto user2 = createParty("USER", "user2");
                PartyDto patient = createParty("PATIENT", "patient");
                PartyDto patient1 = createParty("PATIENT", "patient1");
                PartyDto patient2 = createParty("PATIENT", "patient2");
                PartyDto patient3 = createParty("PATIENT", "patient3");
                PartyDto org = createParty("ORGANIZATION", "org");
                PartyDto org1 = createParty("ORGANIZATION", "org1");
                PartyDto org2 = createParty("ORGANIZATION", "org2");
                PartyDto team = createParty("CARE_TEAM", "team");
                PartyDto team1 = createParty("CARE_TEAM", "team1");
                PartyDto study = createParty("STUDY", "team");
                PartyDto study1 = createParty("STUDY", "study1");

                createPartyRelation(user, patient, "OWNS");
                createPartyRelation(user, patient, "HAS");
                createPartyRelation(user, patient1, "OWNS");
                createPartyRelation(user, patient2, "OWNS");

                createPartyRelation(user1, patient, "HAS");
                createPartyRelation(user1, patient1, "OWNS");
                createPartyRelation(user1, patient2, "OWNS");

                createPartyRelation(user2, patient1, "OWNS");
                createPartyRelation(user2, patient2, "USES");

                createPartyRelation(user2, org2, "MEMBER_OF");
                createPartyRelation(user1, org1, "MEMBER_OF");
                createPartyRelation(user, org, "MEMBER_OF");

                createPartyRelation(org1, patient, "PROVIDES_FOR");
                createPartyRelation(org1, patient1, "PROVIDES_FOR");
                createPartyRelation(org2, patient2, "PROVIDES_FOR");
                createPartyRelation(org2, patient, "PROVIDES_FOR");

                createPartyRelation(user, team, "INCLUDED_IN");
                createPartyRelation(user1, team1, "INCLUDED_IN");

                createPartyRelation(team, study, "ASSIGNED_TO");
                createPartyRelation(team1, study1, "ASSIGNED_TO");

                createPartyRelation(patient, study, "CONSENTS_TO");
                createPartyRelation(patient3, study, "CONSENTS_TO");
                createPartyRelation(patient1, study1, "CONSENTS_TO");
                createPartyRelation(patient2, study1, "CONSENTS_TO");
            };
        }

        private PartyRelationDto createPartyRelation(PartyDto source, PartyDto target, String relationType) {
            PartyRelationDto partyRelationDto = new PartyRelationDto();
            partyRelationDto.setTarget(target.getId());
            partyRelationDto.setSource(source.getId());
            partyRelationDto.setRelationType(relationType);
            return restTemplate.postForEntity(BASE_URL + "/admin/partyRelation", partyRelationDto, PartyRelationDto.class).getBody();
        }

        private PartyDto createParty(String type, String... externaIds) {
            PartyDto partyDto = new PartyDto();
            partyDto.setType(type);
            partyDto.setExternalIds(Arrays.stream(externaIds).collect(Collectors.toSet()));
            return restTemplate.postForEntity(BASE_URL + "/admin/party", partyDto, PartyDto.class).getBody();
        }

        private RelationTypeDto createRelationType(String name, String source, String target) {
            RelationTypeDto relationTypeDto = new RelationTypeDto();
            relationTypeDto.setName(name);
            relationTypeDto.setAllowedSourcePartyType(source);
            relationTypeDto.setAllowedTargetPartyType(target);
            return restTemplate.postForEntity(BASE_URL + "/admin/relationType", relationTypeDto, RelationTypeDto.class).getBody();
        }

        private PartyType createPartyType(String name) {
            PartyType partyType = new PartyType();
            partyType.setName(name);
            return restTemplate.postForEntity(BASE_URL + "/admin/partyType", partyType, PartyType.class).getBody();
        }
    }
}
