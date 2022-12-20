package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyRelationDto;
import care.better.abac.dto.PartyTypeDto;
import care.better.abac.dto.RelationTypeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AbacConfiguration.class)
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class PartyRelationResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/partyRelation";

    private String sourcePartyTypeName;
    private Long sourcePartyId;

    private String targetPartyTypeName;
    private Long targetPartyId;

    private String relationType;

    @BeforeEach
    public void setUp() {
        sourcePartyTypeName = createPartyType(new PartyTypeDto(null, "ROLE")).getName();
        sourcePartyId = createParty(getPartyDto(sourcePartyTypeName, "CLINICIAN")).getId();

        targetPartyTypeName = createPartyType(new PartyTypeDto(null, "FORM")).getName();
        targetPartyId = createParty(getPartyDto(targetPartyTypeName, "Personal information")).getId();

        RelationTypeDto relationTypeDto = getRelationTypeDto("CAN_VIEW");
        relationType = createRelationType(relationTypeDto).getName();
    }

    @Test
    public void createPartyRelation() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        PartyRelationDto dto = getPartyRelationDto(now);

        // when
        PartyRelationDto createdDto = createEntity(BASE_URL, dto);

        // then
        assertThat(createdDto.getSource()).isEqualTo(sourcePartyId);
        assertThat(createdDto.getTarget()).isEqualTo(targetPartyId);
        assertThat(createdDto.getRelationType()).isEqualTo(relationType);
        assertThat(createdDto.getValidUntil()).isEqualTo(now);
    }

    @Test
    public void createPartyRelationIgnoreId() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        PartyRelationDto dto = getPartyRelationDto(now);

        long idParameter = 1000L;
        dto.setId(idParameter);

        // when
        PartyRelationDto createdDto = createEntity(BASE_URL, dto);

        // then
        assertThat(createdDto.getSource()).isEqualTo(sourcePartyId);
        assertThat(createdDto.getTarget()).isEqualTo(targetPartyId);
        assertThat(createdDto.getRelationType()).isEqualTo(relationType);
        assertThat(createdDto.getValidUntil()).isEqualTo(now);

        assertThat(createdDto.getId()).isNotEqualTo(idParameter);
    }

    @Test
    public void updatePartyRelation() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        PartyRelationDto dto = getPartyRelationDto(now);
        Long id = createPartyRelation(dto).getId();
        dto.setValidUntil(now);

        Long sourcePartyId2 = createParty(getPartyDto(sourcePartyTypeName, "URGENT_CARE")).getId();
        Long targetPartyId2 = createParty(getPartyDto(targetPartyTypeName, "Alerts")).getId();
        String relationType2 = createRelationType(getRelationTypeDto("CAN_DELETE")).getName();
        PartyRelationDto updatedPartyRelationDto = new PartyRelationDto(null, sourcePartyId2, relationType2, targetPartyId2);
        OffsetDateTime updatedValidUntil = OffsetDateTime.now().plusYears(1L);
        updatedPartyRelationDto.setValidUntil(updatedValidUntil);

        // when
        PartyRelationDto updatedDto = updateEntity(BASE_URL, id, updatedPartyRelationDto);

        // then
        assertThat(updatedDto.getSource()).isEqualTo(sourcePartyId2);
        assertThat(updatedDto.getTarget()).isEqualTo(targetPartyId2);
        assertThat(updatedDto.getRelationType()).isEqualTo(relationType2);
        assertThat(updatedDto.getValidUntil()).isEqualTo(updatedValidUntil);

        // given
        updatedPartyRelationDto.setValidUntil(null);

        // when
        PartyRelationDto updatedDtoWithRemovedValidUntil = updateEntity(BASE_URL, id, updatedPartyRelationDto);

        // then
        assertThat(updatedDtoWithRemovedValidUntil.getValidUntil()).isNull();
    }

    @Test
    public void updatePartyRelationForUnknownIdFailed() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        PartyRelationDto dto = getPartyRelationDto(now);
        Long id = createPartyRelation(dto).getId();
        dto.setValidUntil(now);

        Long sourcePartyId2 = createParty(getPartyDto(sourcePartyTypeName, "CLERICAL")).getId();
        Long targetPartyId2 = createParty(getPartyDto(targetPartyTypeName, "Emergency care and treatment plan")).getId();
        String relationType2 = createRelationType(getRelationTypeDto("CAN_CREATE")).getName();

        PartyRelationDto updatedPartyRelationDto = new PartyRelationDto(null, sourcePartyId2, relationType2, targetPartyId2);
        OffsetDateTime updatedValidUntil = OffsetDateTime.now().plusYears(1L);
        updatedPartyRelationDto.setValidUntil(updatedValidUntil);

        long unknownId = id + 100L;

        // when
        ResponseEntity<PartyRelationDto> updatedResponse = exchangePut(BASE_URL, unknownId, updatedPartyRelationDto);

        // then
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatedResponse.getBody()).isNull();
    }

    @Test
    public void deletePartyRelation() {
        // given
        Long id = createPartyRelation(getPartyRelationDto(null)).getId();

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, id);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deletePartyRelationForUnknownIdFailed() {
        // given
        long unknownId = Long.MAX_VALUE;

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, unknownId);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private PartyRelationDto getPartyRelationDto(OffsetDateTime validUntil) {
        PartyRelationDto dto = new PartyRelationDto(null, sourcePartyId, relationType, targetPartyId);
        dto.setValidUntil(validUntil);
        return dto;
    }

    private PartyDto getPartyDto(String partyTypeName, String externalId) {
        PartyDto partyDto = new PartyDto(null, partyTypeName);
        partyDto.setExternalIds(Collections.singleton(externalId));
        return partyDto;
    }

    private RelationTypeDto getRelationTypeDto(String name) {
        RelationTypeDto relationTypeDto = new RelationTypeDto(null, name);
        relationTypeDto.setAllowedSourcePartyType(sourcePartyTypeName);
        relationTypeDto.setAllowedTargetPartyType(targetPartyTypeName);
        return relationTypeDto;
    }
}
