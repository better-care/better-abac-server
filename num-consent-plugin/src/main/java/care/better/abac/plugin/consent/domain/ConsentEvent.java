package care.better.abac.plugin.consent.domain;

import care.better.abac.plugin.consent.domain.fhir.ConsentDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.hl7.fhir.r4.model.Consent;

@Data
public class ConsentEvent {

  private Boolean insert;

  @JsonDeserialize(using = ConsentDeserializer.class)
  private Consent consent;
}
