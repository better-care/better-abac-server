package care.better.abac.plugin.sync;

import net.javacrumbs.shedlock.core.LockConfiguration;

/**
 * @author Andrej Dolenc
 */
public interface RunnableWithLockConfiguration extends Runnable {
    LockConfiguration getLockConfiguration();
}
