package cloud.angst.dbcache.exception;

public abstract class DBCacheException extends RuntimeException {
    public DBCacheException(String message) {
        super(message);
    }

    public DBCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
