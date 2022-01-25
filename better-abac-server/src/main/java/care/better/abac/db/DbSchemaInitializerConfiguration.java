
package care.better.abac.db;

import care.better.schema.db.SchemaInitializer;
import care.better.schema.db.impl.SchemaInitializerImpl;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author Gregor Berger
 */
@Configuration
public class DbSchemaInitializerConfiguration {

    @Bean
    @DependsOn("dbSchemaFixer")
    public SchemaInitializer schemaInitializer(DataSource dataSource, String upgradeScriptLocation) throws SQLException, IOException {
        SchemaInitializerImpl schemaInitializer = new SchemaInitializerImpl(
                dataSource,
                getDialect(dataSource).getName(),
                false,
                "schema_version",
                upgradeScriptLocation);
        schemaInitializer.initializeOrUpdate();
        return schemaInitializer;
    }

    @Bean
    public DbSchemaFixer dbSchemaFixer(DataSource dataSource, String upgradeScriptLocation) throws SQLException {
        DbSchemaFixer dbSchemaFixer = new DbSchemaFixer(dataSource, upgradeScriptLocation);
        dbSchemaFixer.fixIfNeeded();
        return dbSchemaFixer;
    }

    @Bean
    public String upgradeScriptLocation(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String name = connection.getMetaData().getDatabaseProductName();
            if (name.startsWith("PostgreSQL")) {
                return "pgsql";
            } else if (name.startsWith("Microsoft SQL Server")) {
                return "mssql2012";
            } else if (name.startsWith("Oracle")) {
                return "ora";
            } else if (name.startsWith("H2")) {
                return "h2";
            } else {
                throw new BeanInitializationException("Database is not supported in this version of ABAC!");
            }
        }
    }

    private Class<? extends Dialect> getDialect(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            StandardDialectResolver dialectResolver = new StandardDialectResolver();
            DatabaseMetaData metaData = connection.getMetaData();
            DatabaseMetaDataDialectResolutionInfoAdapter info = new DatabaseMetaDataDialectResolutionInfoAdapter(metaData);
            Dialect dialect = dialectResolver.resolveDialect(info);
            if (dialect instanceof PostgreSQL95Dialect) {
                return PostgreSQL95Dialect.class;
            } else if (dialect instanceof PostgreSQL82Dialect) {
                return PostgreSQL82Dialect.class;
            } else if (dialect instanceof SQLServer2012Dialect) {
                return SQLServer2012Dialect.class;
            } else if (dialect instanceof SQLServer2008Dialect) {
                return SQLServer2008Dialect.class;
            } else if (dialect instanceof Oracle12cDialect) {
                return Oracle12cDialect.class;
            } else if (dialect instanceof Oracle10gDialect) {
                return Oracle10gDialect.class;
            } else if (dialect instanceof H2Dialect) {
                return H2Dialect.class;
            } else {
                throw new BeanInitializationException("Database not supported: " + metaData.getDatabaseProductName());
            }
        }
    }
}
