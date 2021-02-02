package care.better.abac.plugin.shedlock;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * Only for hibernate schema generation when using locking scheduler
 *
 * @author Andrej Dolenc
 */
@Entity
public class Shedlock {

    @Id
    @Getter
    @Setter
    @Column(nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(nullable = false)
    private OffsetDateTime lockUntil;

    @Getter
    @Setter
    @Column(nullable = false)
    private OffsetDateTime lockedAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private String lockedBy;
}
