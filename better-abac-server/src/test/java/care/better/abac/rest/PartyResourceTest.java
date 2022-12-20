package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PartyDto;
import care.better.abac.dto.PartyTypeDto;
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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Matic Ribic
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {AbacConfiguration.class, PolicyResourceTest.Config.class})
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureTestDatabase
public class PartyResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/party";

    private String partyTypeName;

    @BeforeEach
    public void setUp() {
        partyTypeName = createPartyType(new PartyTypeDto(null, "FORM")).getName();
    }

    @Test
    public void createParty() {
        // given
        String externalId = "Personal information";

        // when
        PartyDto createdDto = createEntity(BASE_URL, getPartyDto(externalId));

        // then
        assertThat(createdDto.getType()).isEqualTo(partyTypeName);
        assertThat(createdDto.getExternalIds()).containsExactly(externalId);
    }

    @Test
    public void createPartyIgnoreId() {
        // given
        String externalId = "Personal information";
        long idParameter = 1000L;
        PartyDto partyDto = new PartyDto(idParameter, partyTypeName);
        partyDto.setExternalIds(Collections.singleton(externalId));

        // when
        PartyDto createdDto = createEntity(BASE_URL, partyDto);

        // then
        assertThat(createdDto.getType()).isEqualTo(partyTypeName);
        assertThat(createdDto.getExternalIds()).containsExactly(externalId);
        assertThat(createdDto.getId()).isNotEqualTo(idParameter);
    }

    @Test
    public void updateParty() {
        // given
        Long id = createParty(getPartyDto("Personal information")).getId();

        createPartyType(new PartyTypeDto(null, "TEMPLATE"));
        long ignoredId = id + 100L;

        PartyDto updatedPartyDto = new PartyDto(ignoredId, "TEMPLATE");
        updatedPartyDto.setExternalIds(Collections.singleton("About me"));

        // when
        PartyDto updatedDto = updateEntity(BASE_URL, id, updatedPartyDto);

        // then
        assertThat(updatedDto.getType()).isEqualTo("TEMPLATE");
        assertThat(updatedDto.getExternalIds()).containsExactly("About me");
    }

    @Test
    public void updatePartyForUnknownIdFailed() {
        // given
        PartyDto partyDto = createParty(getPartyDto("Emergency care and treatment plan"));
        long unknownId = partyDto.getId() + 100L;

        // when
        ResponseEntity<PartyDto> updatedResponse = exchangePut(BASE_URL, unknownId, getPartyDto("Alerts"));

        // then
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatedResponse.getBody()).isNull();
    }

    @Test
    public void deletePartyType() {
        // given
        Long id = createParty(getPartyDto("Advance decisions and statements")).getId();

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, id);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deletePartyTypeForUnknownIdFailed() {
        // given
        long unknownId = Long.MAX_VALUE;

        // when
        ResponseEntity<Void> deleteResponse = exchangeDelete(BASE_URL, unknownId);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private PartyDto getPartyDto(String externalId) {
        PartyDto partyDto = new PartyDto(null, partyTypeName);
        partyDto.setExternalIds(Collections.singleton(externalId));
        return partyDto;
    }
}
