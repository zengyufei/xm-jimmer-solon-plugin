package org.babyfish.jimmer.spring.core.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.babyfish.jimmer.jackson.ImmutableModule;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.runtime.DraftContext;
import org.babyfish.jimmer.runtime.Internal;
import org.babyfish.jimmer.sql.cache.SerializationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class JacksonValueSerializer<T> {

    private static final String NULL_BYTES = "<null>";

    private final ObjectMapper mapper;

    private final JavaType valueType;

    private final boolean requireNewDraftContext;

    public JacksonValueSerializer(@NotNull ImmutableType type) {
        this(type, null, null);
    }

    public JacksonValueSerializer(@NotNull ImmutableProp prop) {
        this(null, prop, null);
    }

    public JacksonValueSerializer(@NotNull ImmutableType type, ObjectMapper mapper) {
        this(type, null, mapper);
    }

    public JacksonValueSerializer(@NotNull ImmutableProp prop, ObjectMapper mapper) {
        this(null, prop, mapper);
    }

    private JacksonValueSerializer(ImmutableType type, ImmutableProp prop, ObjectMapper mapper) {
        if ((type == null) == (prop == null)) {
            throw new IllegalArgumentException("Internal bug: nullity of type and prop must be different");
        }
        ObjectMapper clonedMapper = mapper != null?
            new ObjectMapper(mapper) {} :
            new ObjectMapper().registerModule(new JavaTimeModule());
        clonedMapper.registerModule(new ImmutableModule());
        this.mapper = clonedMapper;
        if (prop == null) {
            this.valueType = SimpleType.constructUnsafe(type.getJavaClass());
        } else if (prop.isAssociation(TargetLevel.ENTITY)) {
            ImmutableProp targetIdProp = prop.getTargetType().getIdProp();
            JavaType targetIdType = SimpleType.constructUnsafe(
                    targetIdProp.getElementClass()
            );
            if (prop.isReferenceList(TargetLevel.OBJECT)) {
                this.valueType = CollectionType.construct(
                        List.class,
                        null,
                        null,
                        null,
                        targetIdType
                );
            } else {
                this.valueType = targetIdType;
            }
        } else {
            this.valueType = SimpleType.constructUnsafe(prop.getElementClass());
        }
        this.requireNewDraftContext = type != null;
    }

    @NotNull
    public String serialize(T value) {
        if (value == null) {
            return NULL_BYTES;
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new SerializationException(ex);
        }
    }

    @NotNull
    public <K> Map<K, String> serialize(@NotNull Map<K, T> map) {
        Map<K, String> serializedMap = new LinkedHashMap<>((map.size() * 4 + 2) / 3);
        for (Map.Entry<K, T> e : map.entrySet()) {
            serializedMap.put(e.getKey(), serialize(e.getValue()));
        }
        return serializedMap;
    }

    @NotNull
    public <K1, K2> Map<K2, String> serialize(@NotNull Map<K1, T> map, @NotNull Function<K1, K2> keyMapper) {
        Map<K2, String> serializedMap = new LinkedHashMap<>((map.size() * 4 + 2) / 3);
        for (Map.Entry<K1, T> e : map.entrySet()) {
            serializedMap.put(keyMapper.apply(e.getKey()), serialize(e.getValue()));
        }
        return serializedMap;
    }

    public T deserialize(String value) {
        if (!requireNewDraftContext) {
            return deserializeImpl(value, null);
        }
        return Internal.requiresNewDraftContext(ctx -> deserializeImpl(value, ctx));
    }

    @NotNull
    public <K> Map<K, T> deserialize(@NotNull Map<K, String> map) {
        Map<K, T> deserializedMap = new LinkedHashMap<>((map.size() * 4 + 2) / 3);
        if (!requireNewDraftContext) {
            for (Map.Entry<K, String> e : map.entrySet()) {
                deserializedMap.put(e.getKey(), deserializeImpl(e.getValue(), null));
            }
        } else {
            Internal.requiresNewDraftContext(ctx -> {
                for (Map.Entry<K, String> e : map.entrySet()) {
                    deserializedMap.put(e.getKey(), deserializeImpl(e.getValue(), ctx));
                }
                return null;
            });
        }
        return deserializedMap;
    }

    @NotNull
    public <K1, K2> Map<K2, T> deserialize(@NotNull Map<K1, String> map, @NotNull Function<K1, K2> keyMapper) {
        Map<K2, T> deserializedMap = new LinkedHashMap<>((map.size() * 4 + 2) / 3);
        if (!requireNewDraftContext) {
            for (Map.Entry<K1, String> e : map.entrySet()) {
                deserializedMap.put(keyMapper.apply(e.getKey()), deserializeImpl(e.getValue(), null));
            }
        } else {
            Internal.requiresNewDraftContext(ctx -> {
                for (Map.Entry<K1, String> e : map.entrySet()) {
                    deserializedMap.put(keyMapper.apply(e.getKey()), deserializeImpl(e.getValue(), ctx));
                }
                return null;
            });
        }
        return deserializedMap;
    }

    @NotNull
    public <K> Map<K, T> deserialize(@NotNull Collection<K> keys, @NotNull Collection<String> values) {
        Map<K, T> deserializedMap = new LinkedHashMap<>((keys.size() * 4 + 2) / 3);
        if (!requireNewDraftContext) {
            Iterator<K> keyItr = keys.iterator();
            Iterator<String> byteArrItr = values.iterator();
            while (keyItr.hasNext() && byteArrItr.hasNext()) {
                K key = keyItr.next();
                String byteArr = byteArrItr.next();
                if (byteArr != null) {
                    deserializedMap.put(key, deserializeImpl(byteArr, null));
                }
            }
        } else {
            Internal.requiresNewDraftContext(ctx -> {
                Iterator<K> keyItr = keys.iterator();
                Iterator<String> byteArrItr = values.iterator();
                while (keyItr.hasNext() && byteArrItr.hasNext()) {
                    K key = keyItr.next();
                    String byteArr = byteArrItr.next();
                    if (byteArr != null) {
                        deserializedMap.put(key, deserializeImpl(byteArr, ctx));
                    }
                }
                return null;
            });
        }
        return deserializedMap;
    }

    private T deserializeImpl(String value, DraftContext ctx) {
        if (value == null || "".equals(value) || NULL_BYTES.equals(value)) {
            return null;
        }
        T deserializedValue;
        try {
            deserializedValue = mapper.readValue(value, valueType);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        }
        return ctx != null ? ctx.resolveObject(deserializedValue) : deserializedValue;
    }

    public JavaType getValueType() {
        return valueType;
    }
}
