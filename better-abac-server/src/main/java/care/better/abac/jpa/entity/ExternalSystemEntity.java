package care.better.abac.jpa.entity;

import care.better.abac.dto.config.ExternalSystemValidationStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Matic Ribic
 */
@Table(name = "external_system", uniqueConstraints = @UniqueConstraint(columnNames = "externalId"))
@Entity
public class ExternalSystemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @NotNull
    private String name;
    @NotNull
    private String externalId;
    @NotNull
    private String configHash;

    private String abacRestBaseUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ExternalSystemValidationStatus validationStatus = ExternalSystemValidationStatus.UNKNOWN;

    @OneToMany(mappedBy = "externalSystem", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ExternalPolicyEntity> policies = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String systemId) {
        this.externalId = systemId;
    }

    public String getConfigHash() {
        return configHash;
    }

    public void setConfigHash(String configHash) {
        this.configHash = configHash;
    }

    public String getAbacRestBaseUrl() {
        return abacRestBaseUrl;
    }

    public void setAbacRestBaseUrl(String abacRestBaseUrl) {
        this.abacRestBaseUrl = abacRestBaseUrl;
    }

    public ExternalSystemValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ExternalSystemValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Set<ExternalPolicyEntity> getPolicies() {
        return policies;
    }

    public void setPolicies(Set<ExternalPolicyEntity> policies) {
        this.policies = policies;
    }
}
