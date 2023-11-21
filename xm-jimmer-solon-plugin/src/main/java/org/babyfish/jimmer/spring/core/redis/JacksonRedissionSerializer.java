package org.babyfish.jimmer.spring.core.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.noear.solon.Solon;
import org.noear.solon.data.cache.Serializer;

public class JacksonRedissionSerializer implements Serializer<String> {
    public static final JacksonRedissionSerializer instance = new JacksonRedissionSerializer();
    public static ObjectMapper objectMapper;

    public JacksonRedissionSerializer() {
        objectMapper = Solon.context().getBean(ObjectMapper.class);
        if (objectMapper == null) {
            Solon.context().getBeanAsync(ObjectMapper.class, bean -> {
                objectMapper = bean;
            });
        }
    }

    public JacksonRedissionSerializer(ObjectMapper objectMapper) {
        JacksonRedissionSerializer.objectMapper = objectMapper;
    }

    public String name() {
        return "json-jackson";
    }

    public String serialize(Object fromObj) throws Exception {
        return objectMapper.writeValueAsString(fromObj);
    }

    public Object deserialize(String dta, Class<?> toClz) throws Exception {
        return objectMapper.readValue(dta, toClz);
    }
}
