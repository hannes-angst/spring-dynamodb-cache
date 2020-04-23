package cloud.angst.dbcache.cache;

import cloud.angst.dbcache.exception.DBCacheEvictException;
import cloud.angst.dbcache.exception.DBCachePutException;
import cloud.angst.dbcache.exception.DBCacheRetrievalException;
import cloud.angst.dbcache.exception.DBCacheValueConversionException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.amazonaws.services.dynamodbv2.document.ItemUtils.toAttributeValue;
import static com.amazonaws.services.dynamodbv2.document.ItemUtils.toSimpleMapValue;
import static com.amazonaws.services.dynamodbv2.util.TableUtils.waitUntilActive;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.singletonList;

class AWSCache {
    private static final Logger logger = LoggerFactory.getLogger(AWSCache.class);
    private static final String KEY_COLUMN = "cache-key";
    private static final String TTL_COLUMN = "cache-ttl";
    private static final String TYPE_COLUMN = "cache-type";
    private static final String VALUE_COLUMN = "cache-value";
    private static final String SIMPLE_TYPE = "simple";
    private static final String COMPLEX_TYPE = "complex";
    private static final DateTimeFormatter FORMATTER = ISO_INSTANT.withZone(ZoneId.systemDefault());

    private final AmazonDynamoDB client;
    private final String tableName;
    private final Class<Object> valueClass;

    public AWSCache(AmazonDynamoDB client, String tableName, Class<Object> value) {
        this.client = client;
        this.tableName = tableName;
        this.valueClass = value;
        logger.info("Cache will operate on table '{}'.", tableName);
    }

    public Object get(@NotNull String key) {
        logger.debug("get '{}.'", key);

        try {
            GetItemResult res = client.getItem(new GetItemRequest()
                    .withTableName(tableName)
                    .withKey(Map.of(KEY_COLUMN, new AttributeValue(key))));

            var item = res.getItem();
            if (item == null || item.isEmpty()) {
                return null;
            }

            Map<String, AttributeValue> entries = new LinkedHashMap<>(item);
            entries.remove(KEY_COLUMN);

            var type = entries.get(TYPE_COLUMN);
            if (type == null) {
                return null;
            }
            boolean isSimple = type.getS().equals(SIMPLE_TYPE);
            entries.remove(TYPE_COLUMN);

            var ttlAttribute = entries.get(TTL_COLUMN);
            if (ttlAttribute != null) {
                Instant eol = parseDate(ttlAttribute.getS());
                if (eol != null && Instant.now().isAfter(eol)) {
                    evict(key);
                    return null;
                }
            }
            entries.remove(TTL_COLUMN);

            if (entries.isEmpty()) {
                return null;
            }

            return fromItem(entries, isSimple, valueClass);
        } catch (AmazonClientException e) {
            logger.error(e.getMessage(), e);
            throw new DBCacheRetrievalException(key, e);
        }
    }


    public void put(@NotNull String key, @NotNull Object value, long ttl) {
        try {
            Map<String, AttributeValue> entry = new HashMap<>();
            entry.put(KEY_COLUMN, new AttributeValue(key));
            if (ttl > 0) {
                entry.put(TTL_COLUMN, new AttributeValue(formatDate(Instant.now().plusMillis(ttl))));
            }

            mapToItem(value, entry);

            logger.debug("put key '{}' with content: {}", key, entry);
            client.putItem(new PutItemRequest()
                    .withTableName(tableName)
                    .withItem(entry));
        } catch (AmazonClientException e) {
            throw new DBCachePutException(key, e);
        }
    }

    public void evict(@NotNull String key) {
        logger.debug("evicting entry '{}'.", key);
        try {
            client.deleteItem(new DeleteItemRequest()
                    .withTableName(tableName)
                    .withKey(Map.of(KEY_COLUMN, new AttributeValue(key)))
                    .addExpectedEntry(KEY_COLUMN, expected(key)));
        } catch (ConditionalCheckFailedException e) {
            logger.debug("Key '{}' not found.", key);
        } catch (AmazonClientException e) {
            throw new DBCacheEvictException(key, e);
        }
    }

    private Instant parseDate(String iso8601) {
        if (iso8601 == null || iso8601.isBlank()) {
            return null;
        }
        try {
            return Instant.from(FORMATTER.parse(iso8601));
        } catch (DateTimeParseException e) {
            logger.error("Could not parse ttl value of '{}'.", iso8601);
        }
        return Instant.now();
    }


    private String formatDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return FORMATTER.format(instant);
    }

    private ExpectedAttributeValue expected(String value) {
        return new ExpectedAttributeValue(true)
                .withValue(new AttributeValue(value));
    }

    public void removeAll() {
        throw new UnsupportedOperationException("Removing all entries from the table is not supported");
    }

    public boolean tableNeedsTobeCreated() {
        try {
            DescribeTableResult tableDescription = client.describeTable(tableName);
            logger.debug("Table '{}' description: {}", tableName, tableDescription.getTable().getTableStatus());
            return false;
        } catch (com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException rnfe) {
            logger.debug("Table '{}' does not exist", tableName);
        }
        return true;
    }

    public void createTable() throws InterruptedException {
        logger.warn("Creating table '{}'.", tableName);
        client.createTable(singletonList(new AttributeDefinition()
                        .withAttributeName(KEY_COLUMN).withAttributeType(
                        ScalarAttributeType.S)),
                tableName, singletonList(new KeySchemaElement()
                        .withAttributeName(KEY_COLUMN)
                        .withKeyType(KeyType.HASH)),
                new ProvisionedThroughput(200L, 200L));

        waitUntilActive(client, tableName, 60000, 1000);
    }


    private Object fromItem(Map<String, AttributeValue> entries, boolean isSimple, Class<Object> valueClass) {
        if (isSimple) {
            return ItemUtils.toSimpleValue(entries.get(VALUE_COLUMN));
        }
        try {
            return mapper.convertValue(toSimpleMapValue(entries), valueClass);
        } catch (Exception e) {
            throw new DBCacheValueConversionException("Could not deserialize object.", e);
        }
    }

    private void mapToItem(@NotNull Object value, Map<String, AttributeValue> entry) {
        try {
            Class<?> valueClass = value.getClass();
            if (SIMPLE_TYPES.contains(valueClass) || valueClass.isArray()) {
                entry.put(TYPE_COLUMN, new AttributeValue(SIMPLE_TYPE));
                entry.put(VALUE_COLUMN, toAttributeValue(value));
            } else if (value instanceof Collection) {
                entry.put(TYPE_COLUMN, new AttributeValue(SIMPLE_TYPE));
                var list = mapper.convertValue(value, ObjectList.class);
                entry.put(VALUE_COLUMN, toAttributeValue(list));
            } else {
                entry.put(TYPE_COLUMN, new AttributeValue(COMPLEX_TYPE));
                var map = mapper.convertValue(value, ObjectMap.class);
                for (var elem : map.entrySet()) {
                    entry.put(elem.getKey(), toAttributeValue(elem.getValue()));
                }
            }
        } catch (Exception e) {
            throw new DBCacheValueConversionException("Could not serialize object.", e);
        }
    }


    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            String.class,
            Float.class,
            Double.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static class ObjectMap extends LinkedHashMap<String, Object> {
    }

    static class ObjectList extends ArrayList<Object> {
    }
}
