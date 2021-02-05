package care.better.abac.plugin.consent.domain.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.hl7.fhir.r4.model.Consent;

public class ConsentDeserializer extends JsonDeserializer<Consent> {
    @Override
    public Consent deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return (Consent) FhirContext.forR4().newJsonParser().parseResource(p.readValueAsTree().toString());
    }
}
