package care.better.abac.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Matic Ribic
 */
@Configuration
public class InitTestEnvConfiguration {

    @Bean
    public InitTestEnvService initTestEnvironmentService() {
        return new InitTestEnvService();
    }
}
