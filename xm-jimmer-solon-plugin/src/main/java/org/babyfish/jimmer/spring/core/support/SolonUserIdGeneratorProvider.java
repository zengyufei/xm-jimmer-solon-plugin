package org.babyfish.jimmer.spring.core.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.DefaultUserIdGeneratorProvider;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.noear.solon.core.AppContext;

import java.util.List;
import java.util.Map;

public class SolonUserIdGeneratorProvider extends DefaultUserIdGeneratorProvider {

    private final AppContext ctx;

    public SolonUserIdGeneratorProvider(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public UserIdGenerator<?> get(Class<UserIdGenerator<?>> type, JSqlClient sqlClient) throws Exception {
        final List<UserIdGenerator<?>> beans = ctx.getBeansOfType(type);
        if (beans.isEmpty()) {
            return super.get(type, sqlClient);
        }
        if (beans.size() > 1) {
            throw new IllegalStateException("Two many spring beans whose type is \"" + type.getName() + "\"");
        }
        return beans.iterator().next();
    }

    @Override
    public UserIdGenerator<?> get(String ref, JSqlClient sqlClient) throws Exception {
        Object bean = ctx.getBean(ref);
        if (!(bean instanceof UserIdGenerator<?>)) {
            throw new IllegalStateException(
                    "The expected type of spring bean named \"ref\" is \"" +
                            UserIdGenerator.class.getName() +
                            "\", but the actual type is + \"" +
                            bean.getClass().getName() +
                            "\""
            );
        }
        return (UserIdGenerator<?>) bean;
    }
}
