package cloud.angst.dbcache.app.config;

import cloud.angst.dbcache.cache.DBCacheManager;
import cloud.angst.dbcache.config.DBCacheProperties;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBCacheConfig {
    @ConfigurationProperties("spring.cache.dbcache")
    public static class CachingProperties extends DBCacheProperties {
    }

    @Bean
    public DBCacheManager cacheManager(AmazonDynamoDBClient client, CachingProperties properties) {
        DBCacheManager cacheManager = new DBCacheManager(client, properties);

        for (var entry : properties.getCache().entrySet()) {
            var key = entry.getKey();
            var valueClass = entry.getValue().getValueClass();
            var ttl = entry.getValue().getTtl();
            cacheManager.addCache(key, valueClass, ttl);
        }

        return cacheManager;
    }
}
