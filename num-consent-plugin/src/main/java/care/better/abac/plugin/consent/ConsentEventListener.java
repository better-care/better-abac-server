package care.better.abac.plugin.consent;

import care.better.abac.plugin.ChangeType;
import care.better.abac.plugin.PartyRelationChange;
import care.better.abac.plugin.RelationType;
import care.better.abac.plugin.consent.domain.ConsentEvent;
import care.better.abac.plugin.listener.Listener;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.DateTimeType;

/** @author Alex Karle */
public class ConsentEventListener implements Listener {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String getId() {
    return "consent";
  }

  @Override
  public Set<PartyRelationChange> processEvent(Object event) {

    ConsentEvent consentEvent = objectMapper.convertValue(event, ConsentEvent.class);

    if (consentEvent == null) {
      return Collections.emptySet();
    }

    if (consentEvent.getInsert() == null) {
      throw new IllegalArgumentException("Unexpected operation");
    }
    if (consentEvent.getInsert()) {
      return createRelationChange(consentEvent.getConsent(), ChangeType.INSERT);
    } else {
      return createRelationChange(consentEvent.getConsent(), ChangeType.DELETE);
    }
  }

  private Set<PartyRelationChange> createRelationChange(Consent consent, ChangeType changeType) {

    Set<String> patients = extractPatientIds(consent);

    Set<String> organizations = extractOrganizationIds(consent);

    Set<String> templateIds = extractTempateIds(consent);

    OffsetDateTime validUntil = extractValidityDate(consent);

    return templateIds.stream()
        .map(
            templateId -> {
              RelationType relationType = new RelationType(templateId, "PATIENT", "ORGANIZATION");
              return new PartyRelationChange(
                  patients, organizations, relationType, changeType, validUntil);
            })
        .collect(Collectors.toSet());
  }

  private Set<String> extractPatientIds(Consent consent) {
    return Set.of(consent.getPatient().getReferenceElement().getIdPart());
  }

  private Set<String> extractTempateIds(Consent consent) {
    return consent.getProvision().getData().stream()
        .map(
            provisionDataComponent ->
                provisionDataComponent.getReference().getIdentifier().getValue())
        .collect(Collectors.toSet());
  }

  private Set<String> extractOrganizationIds(Consent consent) {
    return consent.getOrganization().stream()
        .map(org -> org.getReference().split("/")[1])
        .collect(Collectors.toSet());
  }

  private OffsetDateTime extractValidityDate(Consent consent) {
    DateTimeType endDate = consent.getProvision().getPeriod().getEndElement();

    if (endDate.isEmpty()) {
      return null;
    }

    ZoneOffset zoneOffset;
    if (endDate.hasTime()) {
      zoneOffset = ZoneOffset.ofHoursMinutes(endDate.getTzHour(), endDate.getTzMin());
    } else {
      zoneOffset = ZoneOffset.ofHours(1);
    }

    return OffsetDateTime.ofInstant(endDate.getValue().toInstant(), zoneOffset);
  }
}
