package care.better.abac.plugin;

import lombok.Getter;

/**
 * @author Andrej Dolenc
 */
public enum SynchronizationPhase {
    INITIAL(1000L),
    PERIODIC(10L);

    @Getter
    private final long lockCycleDuration;

    SynchronizationPhase(long lockCycleDuration) {
        this.lockCycleDuration = lockCycleDuration;
    }
}
