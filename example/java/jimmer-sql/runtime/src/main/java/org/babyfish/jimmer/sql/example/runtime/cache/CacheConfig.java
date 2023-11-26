package org.babyfish.jimmer.sql.example.runtime.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.spring.cache.CaffeineBinder;
import org.babyfish.jimmer.spring.core.redis.RedisBinder;
import org.babyfish.jimmer.sql.cache.AbstractCacheFactory;
import org.babyfish.jimmer.sql.cache.Cache;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.cache.chain.ChainCacheBuilder;
import org.babyfish.jimmer.sql.example.model.BookStoreProps;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.cache.redisson.RedissonCacheService;
import org.noear.solon.data.cache.CacheService;

import java.time.Duration;
import java.util.List;

// -----------------------------
// If you are a beginner, please ignore this class,
// for non-cache mode, this class will never be used.
// -----------------------------
@Configuration
public class CacheConfig {

    @Bean(typed = true) //typed 表示可类型注入 //即默认
    public CacheService cache1(@Inject("${demo.cache1}") RedissonCacheService cache) {
        return cache;
    }

    @Bean
    public CacheFactory cacheFactory(
            @Inject CacheService cacheService,
            @Inject ObjectMapper objectMapper
    ) {

        /*
         * Single-view caches:
         *      - All object caches
         *      - `Book.store`
         *      - `Book.authors`
         *      - `TreeNode.parent`
         *      - `TreeNode.childNodes`
         *
         * Multiple-view caches:
         *      - `BookStore.books`
         *      - `Author.books`
         *      - `BookStore.avgPrice`
         *      - `BookStore.newestBooks`
         */
        return new AbstractCacheFactory() {

            // Id -> Object
            @Override
            public Cache<?, ?> createObjectCache(ImmutableType type) { // ❷
                return new ChainCacheBuilder<>()
                        .add(new CaffeineBinder<>(512, Duration.ofSeconds(1)))
                        .add(new RedisBinder<>(cacheService, objectMapper, type, null, Duration.ofMinutes(10), 30))
                        .build();
            }

            // Id -> TargetId, for one-to-one/many-to-one
            @Override
            public Cache<?, ?> createAssociatedIdCache(ImmutableProp prop) { // ❸
                return createPropCache(
                        getFilterState().isAffected(prop.getTargetType()), // ❹
                        prop,
                        cacheService,
                        objectMapper,
                        Duration.ofMinutes(5)
                );
            }

            // Id -> TargetId list, for one-to-many/many-to-many
            @Override
            public Cache<?, List<?>> createAssociatedIdListCache(ImmutableProp prop) { // ❺
                return createPropCache(
                        getFilterState().isAffected(prop.getTargetType()), // ❻
                        prop,
                        cacheService,
                        objectMapper,
                        Duration.ofMinutes(5)
                );
            }

            // Id -> computed value, for transient properties with resolver
            @Override
            public Cache<?, ?> createResolverCache(ImmutableProp prop) { // ❼
                return createPropCache(
                        prop.equals(BookStoreProps.AVG_PRICE.unwrap()) ||
                                prop.equals(BookStoreProps.NEWEST_BOOKS.unwrap()),
                        prop,
                        cacheService,
                        objectMapper,
                        Duration.ofHours(1)
                );
            }
        };
    }

    private static <K, V> Cache<K, V> createPropCache(
            boolean isMultiView,
            ImmutableProp prop,
            CacheService cacheService,
            ObjectMapper objectMapper,
            Duration redisDuration
    ) {
        /*
         * If multi-view cache is required, only redis can be used, because redis support hash structure.
         * The value of redis hash is a nested map, so that different users can see different data.
         *
         * Other simple key value caches can be divided into two levels.
         * The first level is caffeine, the second level is redis.
         *
         * Note: Once the multi-view cache takes affect, it will consume
         * a lot of cache space, please only use it for important data.
         */
        if (isMultiView) { // ❽
            return new ChainCacheBuilder<K, V>()
                .add(new RedisBinder<>(cacheService, objectMapper, null, prop, redisDuration, 30))
                    .build();
        }

        return new ChainCacheBuilder<K, V>()
                .add(new CaffeineBinder<>(512, Duration.ofSeconds(1)))
                .add(new RedisBinder<>(cacheService, objectMapper, null, prop, redisDuration, 30))
                .build();
    }
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/cache/enable-cache
❷ https://babyfish-ct.github.io/jimmer/docs/cache/cache-type/object
❸ ❺ https://babyfish-ct.github.io/jimmer/docs/cache/cache-type/association
❹ ❻ https://babyfish-ct.github.io/jimmer/docs/cache/multiview-cache/user-filter#better-approach
❼ https://babyfish-ct.github.io/jimmer/docs/cache/cache-type/calculation
❽ https://babyfish-ct.github.io/jimmer/docs/cache/multiview-cache/concept
---------------------------------------------------*/
