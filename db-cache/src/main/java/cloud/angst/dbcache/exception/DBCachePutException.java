package cloud.angst.dbcache.exception;

public class DBCachePutException extends DBCacheException {
    private final String key;

    public DBCachePutException(String key, Throwable cause) {
        super("Error storing value of key '" + key + "'.", cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
