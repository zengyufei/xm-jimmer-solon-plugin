package org.babyfish.jimmer.spring.core.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.TransientResolver;
import org.babyfish.jimmer.sql.di.TransientResolverProvider;
import org.noear.solon.core.AppContext;

public class SolonTransientResolverProvider implements TransientResolverProvider {

    private final AppContext ctx;

    public SolonTransientResolverProvider(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public TransientResolver<?, ?> get(
            Class<TransientResolver<?, ?>> type,
            JSqlClient sqlClient
    ) throws Exception {
        return ctx.getBean(type);
    }

    @Override
    public TransientResolver<?, ?> get(String ref, JSqlClient sqlClient) throws Exception {
        Object bean = ctx.getBean(ref);
        if (!(bean instanceof TransientResolver<?, ?>)) {
            throw new IllegalStateException(
                    "The expected type of spring bean named \"ref\" is \"" +
                            TransientResolver.class.getName() +
                            "\", but the actual type is + \"" +
                            bean.getClass().getName() +
                            "\""
            );
        }
        return (TransientResolver<?, ?>) bean;
    }
}
