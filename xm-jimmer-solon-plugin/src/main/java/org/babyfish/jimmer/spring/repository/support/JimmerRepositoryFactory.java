package org.babyfish.jimmer.spring.repository.support;

import org.babyfish.jimmer.spring.core.RepositoryFactorySupport;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.spring.repository.KRepository;
import org.babyfish.jimmer.spring.repository.bytecode.ClassCodeWriter;
import org.babyfish.jimmer.spring.repository.bytecode.JavaClassCodeWriter;
import org.babyfish.jimmer.spring.repository.bytecode.JavaClasses;
import org.babyfish.jimmer.spring.repository.bytecode.KotlinClassCodeWriter;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.kt.KSqlClient;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class JimmerRepositoryFactory extends RepositoryFactorySupport {

    private final Object sqlClient;

    public JimmerRepositoryFactory(Object sqlClient) {
        this.sqlClient = sqlClient;
    }

    /*
     *  // 不兼容 solon
     */
//    @NotNull
//    @Override
//    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
//        return null;
//    }

    /*
     *  // 不兼容 solon
     */
//    @NotNull
//    @Override
//    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
//        return metadata.getRepositoryInterface();
//    }

    /**
     * 兼容 solon 这里重新了 参数列表 Class<?> repositoryInterface, Class<?> domainType
     *
     * @param repositoryInterface
     * @param domainType
     * @return {@link Object}
     */
    @NotNull
    @Override
    /*protected*/ public Object getTargetRepository(Class<?> repositoryInterface, Class<?> domainType) {
        boolean jRepository = JRepository.class.isAssignableFrom(repositoryInterface);
        boolean kRepository = KRepository.class.isAssignableFrom(repositoryInterface);
        if (jRepository && kRepository) {
            throw new IllegalStateException(
                    "Illegal repository interface \"" +
                            repositoryInterface.getName() +
                            "\", it can not extend both \"" +
                            JRepository.class.getName() +
                            "\" and \"" +
                            KRepository.class.getName() +
                            "\""
            );
        }
        if (sqlClient instanceof JSqlClient) {
            if (!jRepository) {
                throw new IllegalStateException(
                        "The type of current sqlClient object is \"" +
                                JSqlClient.class.getName() +
                                "\", but repository interface \"" +
                                repositoryInterface.getName() +
                                "\" does not extend  \"" +
                                JRepository.class.getName() +
                                "\""
                );
            }
        } else if (sqlClient instanceof KSqlClient) {
            if (!kRepository) {
                throw new IllegalStateException(
                        "The type of current sqlClient object is \"" +
                                KSqlClient.class.getName() +
                                "\", but repository interface \"" +
                                repositoryInterface.getName() +
                                "\" does not extend  \"" +
                                KRepository.class.getName() +
                                "\""
                );
            }
        } else {
            throw new IllegalStateException(
                    "Illegal repository interface \"" +
                            repositoryInterface.getName() +
                            "\", it is neither \"" +
                            JRepository.class.getName() +
                            "\" nor \"" +
                            KRepository.class.getName() +
                            "\""
            );
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(
                    ClassCodeWriter.implementationClassName(repositoryInterface),
                    true,
                    repositoryInterface.getClassLoader()
            );
        } catch (ClassNotFoundException ex) {
            // Do nothing
        }
        if (clazz == null) {
//            ClassCodeWriter writer = jRepository ? new JavaClassCodeWriter(metadata) : new KotlinClassCodeWriter(metadata); // 不兼容 solon
            ClassCodeWriter writer = jRepository ? new JavaClassCodeWriter(repositoryInterface, domainType) : new KotlinClassCodeWriter(repositoryInterface, domainType);
            byte[] bytecode = writer.write();
            clazz = JavaClasses.define(bytecode, repositoryInterface);
        }
        try {
            return clazz.getConstructor(jRepository ? JSqlClient.class : KSqlClient.class).newInstance(sqlClient);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new UndeclaredThrowableException(ex.getTargetException(), "Failed to create repository");
        }
    }

}
