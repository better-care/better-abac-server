
package care.better.abac.jpa.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

/**
 * @author Bostjan Lah
 */
@Entity
public class Policy implements EntityWithId, Named {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Integer version;

    @Basic(optional = false)
    @Column(unique = true)
    private String name;
    @Lob
    @Column(nullable = false)
    private String policy;

    public Policy() {
    }

    public Policy(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public String toString() {
        return String.format("Policy[%d:%s]", id, name);
    }
}
