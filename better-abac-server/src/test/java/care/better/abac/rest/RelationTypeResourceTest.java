package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PartyTypeDto;
import care.better.abac.dto.RelationTypeDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AbacConfiguration.class)
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class RelationTypeResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/relationType";

    private String rolePartyType;
    private String formPartyName;

    @Before
    public void setUp() {
        rolePartyType = createPartyType(new PartyTypeDto(null, "ROLE")).getName();
        formPartyName = createPartyType(new PartyTypeDto(null, "FORM")).getName();
    }

    @Test
    public void createRelationType() {
        // given
        String relationName = "CAN_VIEW";

        // when
        RelationTypeDto createdDto = createEntity(BASE_URL, getRelationTypeDto(relationName));

        // then
        assertThat(createdDto.getName()).isEqualTo(relationName);
    }

    @Test
    public void createRelationTypeIgnoreId() {
        // given
        String relationName = "CAN_VIEW";
        long idParameter = 1000L;

        // when
        RelationTypeDto createdDto = createEntity(BASE_URL, getRelationTypeDto(idParameter, relationName));

        // then
        assertThat(createdDto.getName()).isEqualTo(relationName);
        assertThat(createdDto.getId()).isNotEqualTo(idParameter);
    }

    @Test
    public void updateRelationType() {
        // given
        Long id = createRelationType(getRelationTypeDto("CAN_VIEW")).getId();

        long ignoredId = id + 100L;

        RelationTypeDto updatedRelationTypeDto = new RelationTypeDto(ignoredId, "HAS_TEMPLATE");
        updatedRelationTypeDto.setAllowedSourcePartyType("FORM");
        updatedRelationTypeDto.setAllowedTargetPartyType(createPartyType("TEMPLATE").getName());

        // when
        RelationTypeDto updatedDto = updateEntity(BASE_URL, id, updatedRelationTypeDto);

        // then
        assertThat(updatedDto.getName()).isEqualTo("HAS_TEMPLATE");
        assertThat(updatedDto.getAllowedSourcePartyType()).isEqualTo("FORM");
        assertThat(updatedDto.getAllowedTargetPartyType()).isEqualTo("TEMPLATE");
    }

    @Test
    public void updateRelationTypeForUnknownIdFailed() {
        // given
        RelationTypeDto relationTypeDto = createRelationType(getRelationTypeDto("CAN_VIEW"));
        long unknownId = relationTypeDto.getId() + 100L;

        // when
        ResponseEntity<RelationTypeDto> updatedResponse = exchangePut(BASE_URL, unknownId, getRelationTypeDto("CAN_CREATE"));

        // then
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatedResponse.getBody()).isNull();
    }

    @Test
    public void deleteRelationType() {
        // given
        Long id = createRelationType(getRelationTypeDto("CAN_VIEW")).getId();

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, id);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteRelationTypeForUnknownIdFailed() {
        // given
        long unknownId = Long.MAX_VALUE;

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, unknownId);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private RelationTypeDto getRelationTypeDto(String relationName) {
        return getRelationTypeDto(null, relationName);
    }

    private RelationTypeDto getRelationTypeDto(Long id, String relationName) {
        RelationTypeDto relationTypeDto = new RelationTypeDto(id, relationName);
        relationTypeDto.setAllowedSourcePartyType(rolePartyType);
        relationTypeDto.setAllowedTargetPartyType(formPartyName);
        return relationTypeDto;
    }
}