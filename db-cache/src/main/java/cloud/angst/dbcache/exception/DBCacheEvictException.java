package cloud.angst.dbcache.exception;

public class DBCacheEvictException extends DBCacheException {
    private final String key;

    public DBCacheEvictException(String key, Throwable cause) {
        super("Error evicting key '" + key + "'.", cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
