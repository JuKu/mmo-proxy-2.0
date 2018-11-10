package com.jukusoft.mmo.proxy.frontend.database;

import com.jukusoft.mmo.engine.shared.logger.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    //http://www.baeldung.com/hikaricp

    protected static final String LOG_TAG = "Database";

    protected static MySQLConfig mySQLConfig = null;
    protected static HikariDataSource dataSource = null;

    protected Database() {
        //
    }

    public static void init (MySQLConfig mySQLConfig) {
        Database.mySQLConfig = mySQLConfig;

        Log.i(LOG_TAG, "connect to mysql database: " + mySQLConfig.getJDBCUrl());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mySQLConfig.getJDBCUrl());
        config.setUsername(mySQLConfig.getUser());
        config.setPassword(mySQLConfig.getPassword());

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "" + mySQLConfig.getPrepStmtCacheSize());
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "" + mySQLConfig.getPrepStmtCacheSqlLimit());

        config.addDataSourceProperty("useServerPrepStmts", "true");//Newer versions of MySQL support server-side prepared statements, this can provide a substantial performance boost. Set this property to true.

        //recommended default configuration, see https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);

        Log.i(LOG_TAG, "connection established.");
    }

    public static HikariDataSource getDataSource () {
        return dataSource;
    }

    public static Connection getConnection () throws SQLException {
        return dataSource.getConnection();
    }

    public static void close () {
        dataSource.close();
    }

    public static String replacePrefix (String query) {
        return query.replace("{prefix}", mySQLConfig.getPrefix());
    }

}
