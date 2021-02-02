package care.better.abac.plugin.shedlock;

import net.javacrumbs.shedlock.core.LockConfiguration;

/**
 * @author Andrej Dolenc
 */
public interface RunnableWithLockConfiguration extends Runnable {
    LockConfiguration getLockConfiguration();
}
