package care.better.abac;

import care.better.abac.db.DbSchemaInitializerConfiguration;
import care.better.abac.dto.config.ExternalPolicyMapper;
import care.better.abac.dto.config.ExternalSystemMapper;
import care.better.abac.external.ExternalSystemSchedulerConfiguration;
import care.better.abac.external.ExternalSystemService;
import care.better.abac.external.ExternalSystemServiceImpl;
import care.better.abac.external.PartyInfoService;
import care.better.abac.external.demographics.DemographicsPartyInfoService;
import care.better.abac.external.keycloak.KeycloakPartyInfoService;
import care.better.abac.external.noop.NoopPartyInfoService;
import care.better.abac.jpa.QueryDslRepositoryImpl;
import care.better.abac.jpa.repo.ExternalSystemRepository;
import care.better.abac.oauth.OAuth2ClientConfiguration;
import care.better.abac.oauth.OAuth2Configuration;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.policy.config.PolicyConfiguration;
import care.better.abac.rest.client.ExternalSystemRestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bostjan Lah
 */
@Configuration
@ConfigurationProperties(prefix = "partyinfo")
@EnableCaching
@EntityScan(basePackages = "care.better.abac.jpa.entity")
@EnableJpaRepositories(repositoryBaseClass = QueryDslRepositoryImpl.class, basePackages = "care.better.abac.jpa.repo")
@ComponentScan(basePackages = {"care.better.abac.rest", "care.better.abac.audit", "care.better.abac.external", "care.better.abac.plugin"},
               excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
@Import({PluginConfiguration.class,
                PolicyConfiguration.class,
                OAuth2Configuration.class,
                OAuth2ClientConfiguration.class,
                ExternalSystemSchedulerConfiguration.class,
                DbSchemaInitializerConfiguration.class})
public class AbacConfiguration {
    private static final Logger log = LogManager.getLogger(AbacConfiguration.class.getName());

    @NestedConfigurationProperty
    private final List<PartyInfoConf> services = new ArrayList<>();

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "PUT", "DELETE", "POST")
                        .allowedHeaders("*")
                        .allowCredentials(true).maxAge(3600L);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                        .addResourceHandler("/keycloak.json")
                        .addResourceLocations("file:./keycloak.json", "classpath:/keycloak.json");
            }
        };
    }

    @Bean
    public PartyInfoService userPartyInfoService() {
        log.info("services.size = {}", services.size());
        for (PartyInfoConf conf : services) {
            log.info("conf.type = {}", conf.getType());
            if (FixedTypes.USER.name().equals(conf.getType()) && "keycloak".endsWith(conf.getImpl())) {
                return new KeycloakPartyInfoService(conf.getData());
            }
        }
        return new NoopPartyInfoService();
    }

    @Bean
    public PartyInfoService patientPartyInfoService() {
        log.info("services.size = {}", services.size());
        for (PartyInfoConf conf : services) {
            log.info("conf.type = {}", conf.getType());
            if (FixedTypes.PATIENT.name().equals(conf.getType()) && "demographics".endsWith(conf.getImpl())) {
                return new DemographicsPartyInfoService(conf.getData(), "Patient", "maf.fhir.identifier.im");
            }
        }
        return new NoopPartyInfoService();
    }

    @Bean
    public ExternalPolicyMapper externalPolicyMapper() {
        return new ExternalPolicyMapper();
    }

    @Bean
    public ExternalSystemMapper externalSystemMapper(ExternalPolicyMapper externalPolicyMapper) {
        return new ExternalSystemMapper(externalPolicyMapper);
    }

    @Bean
    public ExternalSystemRestClient externalSystemRestClient(ObjectMapper objectMapper) {
        return new ExternalSystemRestClient(objectMapper);
    }

    @Bean
    public ExternalSystemService externalSystemService(
            ExternalSystemRepository externalSystemRepository,
            ExternalSystemMapper mapper,
            ExternalSystemRestClient externalSystemRestClient) {
        return new ExternalSystemServiceImpl(externalSystemRepository, mapper, externalSystemRestClient);
    }

    public List<PartyInfoConf> getServices() {
        return services;
    }

    public static class PartyInfoConf {
        private String type;
        private String impl;
        private String data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getImpl() {
            return impl;
        }

        public void setImpl(String impl) {
            this.impl = impl;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}