package org.babyfish.jimmer.sql.example.runtime.interceptor;

import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.example.business.interceptor.TenantProvider;
import org.babyfish.jimmer.sql.example.model.common.TenantAwareDraft;
import org.babyfish.jimmer.sql.example.model.common.TenantAwareProps;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantAwareDraftInterceptor implements DraftInterceptor<TenantAwareDraft> {

    private final TenantProvider tenantProvider;

    private final String defaultTenant;

    public TenantAwareDraftInterceptor(
            TenantProvider tenantProvider,
            @Value("${demo.default-tenant}") String defaultTenant
    ) {
        this.tenantProvider = tenantProvider;
        this.defaultTenant = defaultTenant;
    }

    @Override
    public void beforeSave(@NotNull TenantAwareDraft draft, boolean isNew) {
        if (!ImmutableObjects.isLoaded(draft, TenantAwareProps.TENANT)) {
            String tenant = tenantProvider.get();
            if (tenant == null) {
                tenant = defaultTenant;
            }
            draft.setTenant(tenant);
        }
    }
}
