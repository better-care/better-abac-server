
package care.better.abac.jpa.entity;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
@Entity
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_party_type"))
    private PartyType type;
    @ElementCollection
    @CollectionTable(name = "external_id", joinColumns = @JoinColumn(name = "party_id"), foreignKey = @ForeignKey(name = "xf_party"))
    @Column(name = "external_id", nullable = false)
    private Set<String> externalIds = new HashSet<>();
    @OneToMany(mappedBy = "source", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<PartyRelation> sourceRelations = new HashSet<>();
    @OneToMany(mappedBy = "target", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<PartyRelation> targetRelations = new HashSet<>();

    public Party() {
    }

    public Party(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public PartyType getType() {
        return type;
    }

    public void setType(PartyType type) {
        this.type = type;
    }

    public Set<String> getExternalIds() {
        return externalIds;
    }

    public Set<PartyRelation> getSourceRelations() {
        return sourceRelations;
    }

    public Set<PartyRelation> getTargetRelations() {
        return targetRelations;
    }

    @Override
    public String toString() {
        return String.format("Party[%d:%s]", id, type);
    }
}
