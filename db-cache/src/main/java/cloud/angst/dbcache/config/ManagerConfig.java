package cloud.angst.dbcache.config;

public class ManagerConfig {
    private String tablePrefix = "cache_";
    private boolean autoCreateCacheTables = false;

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public boolean isAutoCreateCacheTables() {
        return autoCreateCacheTables;
    }

    public void setAutoCreateCacheTables(boolean autoCreateCacheTables) {
        this.autoCreateCacheTables = autoCreateCacheTables;
    }
}
