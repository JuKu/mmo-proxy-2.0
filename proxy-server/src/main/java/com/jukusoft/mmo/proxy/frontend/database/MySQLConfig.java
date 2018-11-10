package com.jukusoft.mmo.proxy.frontend.database;

import com.jukusoft.mmo.engine.shared.config.Config;

public class MySQLConfig {

    protected String host = "locahost";
    protected int port = 3306;
    protected String database = "";
    protected String user = "";
    protected String password = "";
    protected String prefix = "";
    protected int maxPoolSize = 30;

    protected int prepStmtCacheSize = 250;
    protected int prepStmtCacheSqlLimit = 2048;

    public MySQLConfig() {
        //
    }

    public void load () {
        this.host = Config.get("MySQL", "host");
        this.port = Config.getInt("MySQL", "port");
        this.database = Config.get("MySQL", "database");
        this.user = Config.get("MySQL", "user");
        this.password = Config.get("MySQL", "password");
        this.prefix = Config.get("MySQL", "prefix");

        this.maxPoolSize = Config.getInt("MySQL", "max_pool_size");
        this.prepStmtCacheSize = Config.getInt("MySQL", "prepStmtCacheSize");
        this.prepStmtCacheSqlLimit = Config.getInt("MySQL", "prepStmtCacheSqlLimit");
    }

    public String getHost () {
        return this.host;
    }

    public int getPort () {
        return this.port;
    }

    public String getDatabase () {
        return this.database;
    }

    public String getUser () {
        return this.user;
    }

    public String getPassword () {
        return this.password;
    }

    public String getPrefix () {
        return this.prefix;
    }

    public void setPrefix (String prefix) {
        this.prefix = prefix;
    }

    public int getMaxPoolSize () {
        return this.maxPoolSize;
    }

    public String getJDBCUrl () {
        return "jdbc:mysql://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabase() + "?autoreconnect=true&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull";
    }

    public int getPrepStmtCacheSize() {
        return prepStmtCacheSize;
    }

    public int getPrepStmtCacheSqlLimit() {
        return prepStmtCacheSqlLimit;
    }

}
