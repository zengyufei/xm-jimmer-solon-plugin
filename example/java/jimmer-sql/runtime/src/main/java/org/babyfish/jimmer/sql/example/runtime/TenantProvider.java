package org.babyfish.jimmer.sql.example.runtime;

import org.jetbrains.annotations.Nullable;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;

@Component
public class TenantProvider {

    @Nullable
    public String get() {
        final Context current = Context.current();
        if (current != null) {
            String tenant = current.header("tenant");
            return tenant == null || tenant.isEmpty() ? null : tenant;
        }
        return null;
    }
}
