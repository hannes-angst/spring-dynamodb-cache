package cloud.angst.dbcache.config;

import java.time.Duration;

public class DBCacheConfig {
    private Class<Object> valueClass;
    private Duration ttl;

    public Class<Object> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<Object> valueClass) {
        this.valueClass = valueClass;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
