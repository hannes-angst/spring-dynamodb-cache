package cloud.angst.dbcache.cache;

import cloud.angst.dbcache.config.DBCacheProperties;
import cloud.angst.dbcache.exception.DBCacheCreateTableException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

public class DBCacheManager implements CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(DBCacheManager.class);
    private final Map<String, DBCache> caches;
    private final DBCacheProperties properties;
    private final AmazonDynamoDB client;

    public DBCacheManager(AmazonDynamoDB client, DBCacheProperties properties) {
        this.caches = synchronizedMap(new HashMap<>(15));
        this.properties = properties;
        this.client = client;
    }

    @Override
    @Nullable
    public Cache getCache(@NotNull String name) {
        return caches.get(name);
    }

    @NotNull
    @Override
    public Collection<String> getCacheNames() {
        return new HashSet<>(caches.keySet());
    }

    public void addCache(String name, Class<Object> valueClass, Duration ttlDuration) {
        var tableName = createTableName(name);
        var awsCache = new AWSCache(client, tableName, valueClass);
        var ttl = getTTL(properties, ttlDuration);

        createTableIfNeeded(tableName, awsCache);

        logger.debug("Adding new cache '{}' in table '{}' with ttl {} ms.", name, tableName, ttl);

        caches.put(name, new DBCache(name, awsCache, ttl, valueClass));
    }

    @NotNull
    private String createTableName(String name) {
        var tableNameB = new StringBuilder();
        String tablePrefix = properties.getManager().getTablePrefix();
        if (tablePrefix != null && !tablePrefix.isBlank()) {
            tableNameB.append(tablePrefix);
        }

        tableNameB.append(name);

        return tableNameB.toString();
    }

    private void createTableIfNeeded(String tableName, AWSCache awsCache) {
        if (awsCache.tableNeedsTobeCreated()) {
            if (properties.getManager().isAutoCreateCacheTables()) {
                try {
                    awsCache.createTable();
                } catch (InterruptedException e) {
                    throw new DBCacheCreateTableException(tableName, e);
                }
            } else {
                throw new DBCacheCreateTableException(tableName);
            }
        }
    }

    private long getTTL(DBCacheProperties properties, Duration ttlDuration) {
        Duration duration = properties.getTtl();
        if (ttlDuration != null) {
            duration = ttlDuration;
        }

        long ttl = -1;
        if (duration != null) {
            ttl = duration.toMillis();
        }
        if (ttl <= 0) {
            ttl = -1L;
        }
        return ttl;
    }
}
