package pt.iceman.chimerium.config;

public class GeneralConfig {
    private int port;
    private DbConfig dbConfig;

    public GeneralConfig() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
}
