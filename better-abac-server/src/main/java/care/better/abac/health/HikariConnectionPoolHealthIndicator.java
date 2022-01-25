package care.better.abac.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public final class HikariConnectionPoolHealthIndicator implements HealthIndicator {

    private final HikariDataSource hikariDataSource;

    public HikariConnectionPoolHealthIndicator(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public Health health() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName poolAccessor = new ObjectName("com.zaxxer.hikari:type=Pool (" + hikariDataSource.getPoolName() + ')');
            HikariPoolMXBean poolProxy = JMX.newMXBeanProxy(mBeanServer, poolAccessor, HikariPoolMXBean.class);
            String status = poolProxy.getActiveConnections() > poolProxy.getTotalConnections() / 2 ? "DEGRADED" : "UP";
            return Health.status(status)
                    .withDetail("activeConnections", poolProxy.getActiveConnections())
                    .withDetail("maxPoolSize", poolProxy.getTotalConnections())
                    .build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
