package org.babyfish.jimmer.spring.repository.bytecode;

import org.babyfish.jimmer.spring.repository.support.JRepositoryImpl;
import org.babyfish.jimmer.sql.JSqlClient;

import java.lang.reflect.Method;

public class JavaClassCodeWriter extends ClassCodeWriter {

    /**
     * 兼容 solon 这里重新了 参数列表 Class<?> repositoryInterface, Class<?> domainType
     *
     * @param repositoryInterface
     * @param domainType
     */
    public JavaClassCodeWriter(Class<?> repositoryInterface, Class<?> domainType) {
        super(repositoryInterface, domainType, JSqlClient.class, JRepositoryImpl.class);
    }

    @Override
    protected MethodCodeWriter createMethodCodeWriter(Method method, String id) {
        return new JavaMethodCodeWriter(this, method, id);
    }
}
