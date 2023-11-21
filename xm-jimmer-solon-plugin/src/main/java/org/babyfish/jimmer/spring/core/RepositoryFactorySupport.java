package org.babyfish.jimmer.spring.core;

public abstract class RepositoryFactorySupport {
    protected abstract Object getTargetRepository(Class<?> repositoryInterface, Class<?> domainType);
}
