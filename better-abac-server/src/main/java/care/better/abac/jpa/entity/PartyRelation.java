package care.better.abac.jpa.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.OffsetDateTime;

/**
 * @author Bostjan Lah
 */
@Entity
@Table(
        indexes = {
                @Index(name = "xp_party_source", columnList = "source_id"),
                @Index(name = "xp_party_target", columnList = "target_id"),
                @Index(name = "xp_party_type", columnList = "relation_type_id"),
                @Index(name = "xp_party_multi", columnList = "source_id, relation_type_id, target_id")
        }
)
public class PartyRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_relation_source"))
    private Party source;
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_relation_target"))
    private Party target;
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "xf_relation_type"))
    private RelationType relationType;
    @Basic
    private OffsetDateTime validUntil;

    public PartyRelation() {
    }

    public PartyRelation(Party source, Party target, RelationType relationType) {
        this.target = target;
        this.relationType = relationType;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public Party getSource() {
        return source;
    }

    public void setSource(Party source) {
        this.source = source;
    }

    public Party getTarget() {
        return target;
    }

    public void setTarget(Party target) {
        this.target = target;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public OffsetDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return String.format("PartyRelation[%d/%s/%d]", source.getId(), relationType.getName(), target.getId());
    }
}
