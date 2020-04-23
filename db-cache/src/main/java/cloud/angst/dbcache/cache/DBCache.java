package cloud.angst.dbcache.cache;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

public class DBCache implements Cache {
    private final String name;
    private final AWSCache cache;
    private final Class<Object> valueClass;
    private final long ttl;

    DBCache(@NotNull String name,
            @NotNull AWSCache cache,
            long ttl,
            @NotNull Class<Object> valueClass) {
        this.name = name;
        this.ttl = ttl;
        this.cache = cache;
        this.valueClass = valueClass;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(@NotNull Object key) {
        Object value = cache.get(String.valueOf(key));
        if (value == null) {
            return null;
        }
        return new SimpleValueWrapper(value);
    }

    @Override
    public <T> T get(@NotNull Object key, Class<T> type) {
        var value = cache.get(String.valueOf(key));
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull Object key, @NotNull Callable<T> valueLoader) {
        Object value = cache.get(String.valueOf(key));
        if (value == null) {
            try {
                value = valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
            put(key, value);
        }
        return (T) value;
    }

    @Override
    public void put(@NotNull Object key, Object value) {
        cache.put(String.valueOf(key), valueClass.cast(value), ttl);
    }

    @Override
    public void evict(@NotNull Object key) {
        cache.evict(String.valueOf(key));
    }

    @Override
    public void clear() {
        cache.removeAll();
    }
}
