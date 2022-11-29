package care.better.abac;

import care.better.abac.content.AppContentService;
import care.better.abac.content.AppContentServiceImpl;
import care.better.abac.content.AppContentSyncConfiguration;
import care.better.abac.content.AppContentSyncStep;
import care.better.abac.content.AppContentSyncStepActionType;
import care.better.abac.content.AppContentSyncStepDefinition;
import care.better.abac.db.DbSchemaInitializerConfiguration;
import care.better.abac.dto.config.ExternalPolicyMapper;
import care.better.abac.dto.config.ExternalSystemMapper;
import care.better.abac.dto.content.PlainDto;
import care.better.abac.dto.content.PlainPartyDto;
import care.better.abac.dto.content.PlainPartyRelationDto;
import care.better.abac.dto.content.PlainPartyTypeDto;
import care.better.abac.dto.content.PlainPolicyDto;
import care.better.abac.dto.content.PlainRelationTypeDto;
import care.better.abac.external.ExternalSystemSchedulerConfiguration;
import care.better.abac.external.ExternalSystemService;
import care.better.abac.external.ExternalSystemServiceImpl;
import care.better.abac.external.PartyInfoService;
import care.better.abac.external.demographics.DemographicsPartyInfoService;
import care.better.abac.external.keycloak.KeycloakPartyInfoService;
import care.better.abac.external.noop.NoopPartyInfoService;
import care.better.abac.health.HikariConnectionPoolHealthIndicator;
import care.better.abac.jpa.QueryDslRepositoryImpl;
import care.better.abac.jpa.entity.EntityWithId;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.entity.Policy;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.ExternalSystemRepository;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import care.better.abac.oauth.OAuth2ClientConfiguration;
import care.better.abac.oauth.OAuth2Configuration;
import care.better.abac.party.PartySyncStep;
import care.better.abac.party.PartyTypeSyncStep;
import care.better.abac.plugin.config.PluginConfiguration;
import care.better.abac.policy.config.PolicyConfiguration;
import care.better.abac.relations.PartyRelationSyncStep;
import care.better.abac.relations.RelationTypeSyncStep;
import care.better.abac.rest.client.ExternalSystemRestClient;
import care.better.abac.version.VersionProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
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
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
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
        DbSchemaInitializerConfiguration.class,
        AppContentSyncConfiguration.class})
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
    public AppContentSyncStep<PlainPartyDto, Party> partySyncStep(PartyRepository partyRepository) {
        return new PartySyncStep(partyRepository);
    }

    @Bean
    public AppContentSyncStep<PlainPartyTypeDto, PartyType> partyTypeSyncStep(PartyTypeRepository partyTypeRepository) {
        return new PartyTypeSyncStep(partyTypeRepository);
    }

    @Bean
    public AppContentSyncStep<PlainPartyRelationDto, PartyRelation> partyRelationSyncStep(PartyRelationRepository partyRelationRepository) {
        return new PartyRelationSyncStep(partyRelationRepository);
    }

    @Bean
    public AppContentSyncStep<PlainRelationTypeDto, RelationType> relationTypeSyncStep(RelationTypeRepository relationTypeRepository) {
        return new RelationTypeSyncStep(relationTypeRepository);
    }

    @Bean
    public AppContentService appContentService(
            AppContentSyncStep<PlainPartyTypeDto, PartyType> partyTypeSyncStep,
            AppContentSyncStep<PlainPartyDto, Party> partySyncStep,
            AppContentSyncStep<PlainPartyRelationDto, PartyRelation> partyRelationSyncStep,
            AppContentSyncStep<PlainPolicyDto, Policy> policySyncStep,
            AppContentSyncStep<PlainRelationTypeDto, RelationType> relationTypeSyncStep) {
        List<AppContentSyncStep<? extends PlainDto, ? extends EntityWithId>> retrieveSteps = Arrays.asList(partyTypeSyncStep,
                                                                                                           partySyncStep,
                                                                                                           relationTypeSyncStep,
                                                                                                           partyRelationSyncStep,
                                                                                                           policySyncStep);

        List<AppContentSyncStepDefinition<? extends PlainDto, ? extends EntityWithId>> submitStepDefinitions = Arrays.asList(
                new AppContentSyncStepDefinition<>(partyTypeSyncStep, AppContentSyncStepActionType.SUBMIT_WITH_LAZY_DELETE),
                new AppContentSyncStepDefinition<>(partySyncStep, AppContentSyncStepActionType.SUBMIT_WITH_LAZY_DELETE),
                new AppContentSyncStepDefinition<>(relationTypeSyncStep, AppContentSyncStepActionType.SUBMIT_WITH_LAZY_DELETE),
                new AppContentSyncStepDefinition<>(partyRelationSyncStep, AppContentSyncStepActionType.SUBMIT),
                new AppContentSyncStepDefinition<>(relationTypeSyncStep, AppContentSyncStepActionType.DELETE),
                new AppContentSyncStepDefinition<>(partySyncStep, AppContentSyncStepActionType.DELETE),
                new AppContentSyncStepDefinition<>(partyTypeSyncStep, AppContentSyncStepActionType.DELETE),
                new AppContentSyncStepDefinition<>(policySyncStep, AppContentSyncStepActionType.SUBMIT)
                );

        return new AppContentServiceImpl(retrieveSteps, submitStepDefinitions);
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

    @Bean
    public MBeanExporter dataSourceMBeanExporter() {
        MBeanExporter mBeanExporter = new MBeanExporter();
        mBeanExporter.setAutodetect(true);
        mBeanExporter.setExcludedBeans("dataSource");

        return mBeanExporter;
    }

    @Bean
    public HikariConnectionPoolHealthIndicator hikariConnectionPoolHealthIndicator(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            return new HikariConnectionPoolHealthIndicator((HikariDataSource)dataSource);
        }
        return null;
    }

    @Bean
    public VersionProvider versionProvider() {
        return new VersionProvider();
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