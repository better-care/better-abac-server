package care.better.abac.jpa.entity;

import care.better.abac.dto.config.ExternalPolicyType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 * @author Matic Ribic
 */
@Table(name = "external_policy", uniqueConstraints = @UniqueConstraint(columnNames = {"external_system_id", "externalId"}))
@Entity
public class ExternalPolicyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @NotNull
    private String externalId;
    @NotNull
    private String name;
    @NotNull
    private ExternalPolicyType type;
    @NotNull
    @Lob
    private String config;

    @ManyToOne(optional = false)
    private ExternalSystemEntity externalSystem;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExternalPolicyType getType() {
        return type;
    }

    public void setType(ExternalPolicyType type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public ExternalSystemEntity getExternalSystem() {
        return externalSystem;
    }

    public void setExternalSystem(ExternalSystemEntity externalSystem) {
        this.externalSystem = externalSystem;
    }
}
