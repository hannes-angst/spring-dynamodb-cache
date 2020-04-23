package cloud.angst.dbcache.exception;

public class DBCacheCreateTableException extends DBCacheException {
    private final String tableName;

    public DBCacheCreateTableException(String tableName) {
        super("Cache table '" + tableName + "' does not exist.");
        this.tableName = tableName;
    }

    public DBCacheCreateTableException(String tableName, Throwable cause) {
        super("Cache table '" + tableName + "' does not exist.", cause);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
