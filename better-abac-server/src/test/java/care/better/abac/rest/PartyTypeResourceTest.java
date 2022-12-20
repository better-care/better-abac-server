package care.better.abac.rest;

import care.better.abac.AbacConfiguration;
import care.better.abac.dto.PartyTypeDto;
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
public class PartyTypeResourceTest extends AbstractResourceTest {
    static final String BASE_URL = "/rest/v1/admin/partyType";

    @Test
    public void createPartyType() {
        // given
        String name = "FORM";

        // when
        PartyTypeDto createdDto = createEntity(BASE_URL, new PartyTypeDto(null, name));

        // then
        assertThat(createdDto.getName()).isEqualTo(name);
    }

    @Test
    public void createPartyTypeIgnoreId() {
        // given
        long idParameter = 1000L;

        // when
        PartyTypeDto createdDto = createEntity(BASE_URL, new PartyTypeDto(idParameter, "TEMPLATE"));

        // then
        assertThat(createdDto.getName()).isEqualTo("TEMPLATE");
        assertThat(createdDto.getId()).isNotEqualTo(idParameter);
    }

    @Test
    public void updatePartyType() {
        // given
        PartyTypeDto partyTypeDto = createPartyType(new PartyTypeDto(null, "TEMPLATE"));
        Long id = partyTypeDto.getId();
        long ignoredId = id + 100L;

        // when
        PartyTypeDto updatedDto = updateEntity(BASE_URL, id, new PartyTypeDto(ignoredId, "FORM"));

        // then
        assertThat(updatedDto.getName()).isEqualTo("FORM");
        assertThat(updatedDto.getId()).isEqualTo(id);
    }

    @Test
    public void updatePartyTypeForUnknownIdFailed() {
        // given
        PartyTypeDto partyTypeDto = createPartyType(new PartyTypeDto(null, "TEMPLATE"));
        Long id = partyTypeDto.getId();
        long unknownId = id + 100L;

        // when
        ResponseEntity<PartyTypeDto> updatedResponse = exchangePut(BASE_URL, unknownId, new PartyTypeDto(null, "FORM"));

        // then
        assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatedResponse.getBody()).isNull();
    }

    @Test
    public void deletePartyType() {
        // given
        Long id = createPartyType(new PartyTypeDto(null, "FORM")).getId();

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
}
