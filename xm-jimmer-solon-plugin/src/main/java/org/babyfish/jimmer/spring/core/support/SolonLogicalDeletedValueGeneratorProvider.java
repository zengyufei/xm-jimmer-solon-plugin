package org.babyfish.jimmer.spring.core.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.LogicalDeletedValueGeneratorProvider;
import org.babyfish.jimmer.sql.meta.LogicalDeletedValueGenerator;
import org.noear.solon.core.AppContext;

public class SolonLogicalDeletedValueGeneratorProvider implements LogicalDeletedValueGeneratorProvider {

    private final AppContext ctx;

    public SolonLogicalDeletedValueGeneratorProvider(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public LogicalDeletedValueGenerator<?> get(String ref, JSqlClient sqlClient) throws Exception {
        Object bean = ctx.getBean(ref);
        if (!(bean instanceof LogicalDeletedValueGenerator<?>)) {
            throw new IllegalStateException(
                    "The expected type of spring bean named \"ref\" is \"" +
                            LogicalDeletedValueGenerator.class.getName() +
                            "\", but the actual type is + \"" +
                            bean.getClass().getName() +
                            "\""
            );
        }
        return (LogicalDeletedValueGenerator<?>) bean;
    }
}
