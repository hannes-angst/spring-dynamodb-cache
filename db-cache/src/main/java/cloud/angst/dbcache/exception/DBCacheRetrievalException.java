package cloud.angst.dbcache.exception;

public class DBCacheRetrievalException extends DBCacheException {
    private final String key;

    public DBCacheRetrievalException(String key, Throwable cause) {
        super("Error retrieving key '" + key + "'.", cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
