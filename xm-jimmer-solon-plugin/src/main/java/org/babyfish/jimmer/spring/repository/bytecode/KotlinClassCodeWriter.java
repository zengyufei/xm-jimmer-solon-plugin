package org.babyfish.jimmer.spring.repository.bytecode;

import org.babyfish.jimmer.spring.repository.support.KRepositoryImpl;
import org.babyfish.jimmer.sql.kt.KSqlClient;

import java.lang.reflect.Method;

public class KotlinClassCodeWriter extends ClassCodeWriter {

    /**
     * 兼容 solon 这里重新了 参数列表 Class<?> repositoryInterface, Class<?> domainType
     *
     * @param repositoryInterface
     * @param domainType
     */
    public KotlinClassCodeWriter(Class<?> repositoryInterface, Class<?> domainType) {
        super(repositoryInterface, domainType, KSqlClient.class, KRepositoryImpl.class);
    }

    @Override
    protected MethodCodeWriter createMethodCodeWriter(Method method, String id) {
        return new KotlinMethodCodeWriter(this, method, id);
    }
}
