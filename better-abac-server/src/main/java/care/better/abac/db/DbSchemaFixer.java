package care.better.abac.db;

import care.better.schema.db.exception.DatabaseUpgradeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Used to fix database schema, when it was initialized with Hibernate and not with thinkehr-db
 */
public class DbSchemaFixer {
    private final Logger log = LogManager.getLogger(DbSchemaFixer.class);

    private final DataSource dataSource;
    private final String schemaName;
    private static final String SCHEMA_VERSION_TABLE_NAME = "schema_version";
    private static final String EXISTING_TABLE_NAME = "party";

    public DbSchemaFixer(DataSource dataSource, String schemaName) {
        this.dataSource = dataSource;
        this.schemaName = schemaName;
    }

    public void fixIfNeeded() throws SQLException {
        DatabaseMetaData dbMetaData = getDbMetaData(dataSource);
        if (tableExists(dbMetaData, SCHEMA_VERSION_TABLE_NAME)) {
            return;
        }
        if (tableExists(dbMetaData, EXISTING_TABLE_NAME)) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                createSchemaVersionTable(connection);
                connection.commit();
            } catch (SQLException | DatabaseUpgradeException e) {
                if (connection != null) {
                    connection.rollback();
                }
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    private DatabaseMetaData getDbMetaData(DataSource dataSource) throws SQLException {
        return dataSource.getConnection().getMetaData();

    }

    private boolean tableExists(DatabaseMetaData dbMetaData, String tableName) throws SQLException {
        return (schemaName.startsWith("ora") || schemaName.startsWith("h2") ? dbMetaData.getTables(null, null, tableName.toUpperCase(), null)
                : dbMetaData.getTables(null, null, tableName, null)).next();
    }

    private void createSchemaVersionTable(Connection connection) throws SQLException {
        log.info("Switching to automatic schema management. Initializing schema_version table.");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + SCHEMA_VERSION_TABLE_NAME + " (version INTEGER NOT NULL)");
            statement.executeUpdate("INSERT INTO " + SCHEMA_VERSION_TABLE_NAME + " (version) VALUES (1)");
        }
    }
}