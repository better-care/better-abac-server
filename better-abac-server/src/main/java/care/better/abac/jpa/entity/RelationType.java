package care.better.abac.jpa.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * @author Bostjan Lah
 */
@Entity
public class RelationType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @Basic(optional = false)
    @Column(unique = true)
    private String name;
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_pt_allowed_source"))
    private PartyType allowedSource;
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_pt_allowed_target"))
    private PartyType allowedTarget;

    public RelationType() {
    }

    public RelationType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartyType getAllowedSource() {
        return allowedSource;
    }

    public void setAllowedSource(PartyType allowedSource) {
        this.allowedSource = allowedSource;
    }

    public PartyType getAllowedTarget() {
        return allowedTarget;
    }

    public void setAllowedTarget(PartyType allowedTarget) {
        this.allowedTarget = allowedTarget;
    }

    @Override
    public String toString() {
        return String.format("RelationType[%d:%s]", id, name);
    }
}
