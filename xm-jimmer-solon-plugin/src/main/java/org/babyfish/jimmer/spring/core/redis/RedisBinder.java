package org.babyfish.jimmer.spring.core.redis;

import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.cache.chain.SimpleBinder;
import org.noear.solon.data.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RedisBinder<K, V>
        implements SimpleBinder<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBinder.class);

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    private final String keyPrefix;

    private final long minMills;

    private final long maxMillis;

    private final JacksonValueSerializer<V> valueSerializer;

    public RedisBinder(
            CacheService cacheService,
            ObjectMapper objectMapper,
            ImmutableType type,
            ImmutableProp prop,
            Duration duration,
            int randomPercent
    ) {
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        if ((type == null) == (prop == null)) {
            throw new IllegalArgumentException("The nullity of type and prop cannot be same");
        }
        if (type != null) {
            valueSerializer = new JacksonValueSerializer<>(type, objectMapper);
        } else {
            valueSerializer = new JacksonValueSerializer<>(prop, objectMapper);
        }
        if (randomPercent < 0 || randomPercent > 99) {
            throw new IllegalArgumentException("randomPercent must between 0 and 99");
        }
        if (type != null) {
            this.keyPrefix = getKeyPrefix(type);
        } else {
            this.keyPrefix = getKeyPrefix(prop);
        }
        long millis = duration.toMillis();
        minMills = millis - randomPercent * millis / 100;
        maxMillis = millis + randomPercent * millis / 100;
    }


    @Override
    public Map<K, V> getAll(Collection<K> keys) {
        Collection<String> redisKeys = redisKeys(keys);
        List<String> values = read(redisKeys);
        return valueSerializer.deserialize(keys, values);
    }


    protected List<String> read(Collection<String> keys) {
        return keys.stream().map(cacheService::get)
                .map(Convert::toStr).collect(Collectors.toList());
    }

    @Override
    public void setAll(Map<K, V> map) {
        Map<String, String> convertedMap = valueSerializer.serialize(map, this::redisKey);
        for (Map.Entry<String, String> e : convertedMap.entrySet()) {
            final String key = e.getKey();
            final String value = e.getValue();
            cacheService.store(
                    key, value, Convert.toInt(nextExpireMillis() * 1000)
            );
        }
    }


    protected String reason() {
        return "redis";
    }

    @Override
    public void deleteAll(Collection<K> keys, Object reason) {
        if (reason == null || reason.equals(this.reason())) {
            Collection<String> redisKeys = redisKeys(keys);
            delete(redisKeys);
        }
    }

    protected void delete(Collection<String> keys) {
        LOGGER.info("Delete data from redis: {}", keys);
        for (String key : keys) {
            cacheService.remove(key);
        }
    }

    String redisKey(K key) {
        return keyPrefix + key;
    }

    List<String> redisKeys(Collection<K> keys) {
        if (!(keys instanceof Set<?>)) {
            keys = new LinkedHashSet<>(keys);
        }
        return keys.stream().map(this::redisKey).collect(Collectors.toList());
    }


    protected String getKeyPrefix(ImmutableType type) {
        return type.getJavaClass().getSimpleName() + '-';
    }

    protected String getKeyPrefix(ImmutableProp prop) {
        return prop.getDeclaringType().getJavaClass().getSimpleName() + '.' + prop.getName() + '-';
    }

    protected long nextExpireMillis() {
        return ThreadLocalRandom.current().nextLong(minMills, maxMillis);
    }

}
