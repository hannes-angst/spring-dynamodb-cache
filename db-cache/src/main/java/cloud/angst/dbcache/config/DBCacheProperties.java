package cloud.angst.dbcache.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DBCacheProperties {
    private ManagerConfig manager = new ManagerConfig();
    private Map<String, DBCacheConfig> cache = new HashMap<>();
    private Duration ttl;

    public ManagerConfig getManager() {
        return manager;
    }

    public void setManager(ManagerConfig manager) {
        this.manager = manager;
    }

    public Map<String, DBCacheConfig> getCache() {
        return cache;
    }

    public void setCache(Map<String, DBCacheConfig> cache) {
        this.cache = cache;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }


}
