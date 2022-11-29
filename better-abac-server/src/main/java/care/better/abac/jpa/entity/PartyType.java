
package care.better.abac.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
@Entity
public class PartyType implements EntityWithId, Named {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @Basic
    @Column(unique = true)
    private String name;
    @OneToMany(orphanRemoval = true, mappedBy = "type")
    private Set<Party> parties = new HashSet<>();
    @OneToMany(mappedBy = "allowedSource")
    private Set<RelationType> allowedSourceRelations = new HashSet<>();
    @OneToMany(mappedBy = "allowedTarget")
    private Set<RelationType> allowedTargetRelations = new HashSet<>();

    public PartyType() {
    }

    public PartyType(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Set<Party> getParties() {
        return parties;
    }

    @Override
    public String toString() {
        return String.format("PartyType[%d:%s]", id, name);
    }
}
